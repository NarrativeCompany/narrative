package org.narrative.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.narrative.config.properties.NarrativeProperties;
import org.narrative.network.core.security.EmailAndPasswordAuthenticationFilter;
import org.narrative.network.core.security.NarrativeUserDetailsService;
import org.narrative.network.core.security.RestCsrfFilter;
import org.narrative.network.core.security.TwoFactorCodeAuthenticationFilter;
import org.narrative.network.core.security.jwt.JwtAuthenticationTokenFilter;
import org.narrative.network.core.security.jwt.JwtUtil;
import org.narrative.network.core.security.jwt.RestAuthenticationEntryPoint;
import org.narrative.network.customizations.narrative.security.DataVisibilityFilter;
import org.narrative.network.customizations.narrative.service.api.TwoFactorAuthenticationService;
import org.narrative.network.customizations.narrative.service.impl.StaticMethodWrapper;
import org.narrative.network.customizations.narrative.service.impl.common.ValidationExceptionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.firewall.StrictHttpFirewall;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.servlet.HandlerExceptionResolver;

import javax.servlet.http.HttpServletRequest;

/**
 * Override default Spring Security behavior.
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(securedEnabled = true, prePostEnabled = true, jsr250Enabled = true)
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    private final NarrativeUserDetailsService userDetailsService;
    private final RestAuthenticationEntryPoint unauthorizedHandler;
    private final NarrativeProperties narrativeProperties;
    private final HandlerExceptionResolver resolver;
    private final JwtUtil jwtUtil;
    private final MessageSourceAccessor messageSource;
    private final TwoFactorAuthenticationService tfaService;
    private final ValidationExceptionFactory validationExceptionFactory;
    private final ObjectMapper objectMapper;
    private final StaticMethodWrapper staticMethodWrapper;

    public SecurityConfiguration(NarrativeUserDetailsService userDetailsService,
                                 RestAuthenticationEntryPoint unauthorizedHandler, NarrativeProperties narrativeProperties,
                                 HandlerExceptionResolver resolver, JwtUtil jwtUtil, MessageSourceAccessor messageSource,
                                 TwoFactorAuthenticationService tfaService, ValidationExceptionFactory validationExceptionFactory,
                                 ObjectMapper objectMapper, StaticMethodWrapper staticMethodWrapper) {
        this.userDetailsService = userDetailsService;
        this.unauthorizedHandler = unauthorizedHandler;
        this.narrativeProperties = narrativeProperties;
        this.resolver = resolver;
        this.jwtUtil = jwtUtil;
        this.messageSource = messageSource;
        this.tfaService = tfaService;
        this.validationExceptionFactory = validationExceptionFactory;
        this.objectMapper = objectMapper;
        this.staticMethodWrapper = staticMethodWrapper;
    }

    @Override
    public void configure(WebSecurity web) throws Exception {
        StrictHttpFirewall firewall = new StrictHttpFirewall();
        // bl: for legacy Struts UI support, we need to allow URL-encoded slashes for now.
        firewall.setAllowUrlEncodedSlash(true);
        // bl: also need to allow encoded percent for unsubscribe emails, which have email addresses in them
        // and will need to have encoded percents when embedded as a redirect URL for login!login (refer #1174)
        // can be reverted once #835 is done.
        firewall.setAllowUrlEncodedPercent(true);

        web
                .httpFirewall(firewall)
                // bl: ignore requests for the cluster port. this ensures the HttpSecurity configured below
                // will _not_ be applied to cluster requests. we used to have issues with cluster pages
                // being broken when you had a JWT set for the front end (all requests on localhost), so
                // this effectively fixes that issue.
                .ignoring().requestMatchers(forPort(narrativeProperties.getCluster().getPort()));
    }

    @Override
    protected void configure(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                .csrf().disable()
                    .exceptionHandling()
                        .authenticationEntryPoint(unauthorizedHandler)
            //TODO: This is temporary to allow the Struts UI to work - rip me out when we are no longer using Struts
            .and()
                .requestMatchers()
                // bl: only match requests that follow the MVC base URI and the webhooks base URI
                .antMatchers(
                        narrativeProperties.getSpring().getMvc().getBaseUri() + "**",
                        narrativeProperties.getSpring().getMvc().getWebhooksBaseUri() + "**"
                )
            .and()
                .headers()
                .frameOptions().disable()
            .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                // ChangeSessionIdAuthenticationStrategy causes issues with session IDs changing rapidly,
                // which was breaking /api requests if you had visited the struts UI, which uses sessions.
                // disabling session fixation security now since we don't need it, and it solves the problem.
                .sessionFixation().none()
            .and()
                .authorizeRequests()
                .antMatchers().permitAll()
                    .anyRequest().authenticated()
            .and()
                .anonymous().disable()
                .formLogin()
                    .loginProcessingUrl(narrativeProperties.getSecurity().getLoginURI())
                    .permitAll()
                .failureHandler(new SimpleUrlAuthenticationFailureHandler())
            .and()
                .logout()
        ;

        // Adding custom filters.
        AuthenticationManager authenticationManager = authenticationManager();
        JwtAuthenticationTokenFilter jwtAuthenticationTokenFilter = new JwtAuthenticationTokenFilter(jwtUtil, userDetailsService, resolver);
        EmailAndPasswordAuthenticationFilter emailAndPasswordAuthenticationFilter = new EmailAndPasswordAuthenticationFilter(authenticationManager, narrativeProperties, resolver, jwtUtil, messageSource, objectMapper, staticMethodWrapper);
        TwoFactorCodeAuthenticationFilter twoFactorCodeAuthenticationFilter = new TwoFactorCodeAuthenticationFilter(jwtUtil, narrativeProperties, resolver, validationExceptionFactory, tfaService, objectMapper, staticMethodWrapper);
        RestCsrfFilter restCsrfFilter = new RestCsrfFilter(narrativeProperties, resolver);
        DataVisibilityFilter dataVisibilityFilter = new DataVisibilityFilter(staticMethodWrapper);

        httpSecurity
                .addFilterBefore(emailAndPasswordAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(twoFactorCodeAuthenticationFilter, emailAndPasswordAuthenticationFilter.getClass())
                .addFilterBefore(jwtAuthenticationTokenFilter, BasicAuthenticationFilter.class)
                .addFilterAfter(restCsrfFilter, jwtAuthenticationTokenFilter.getClass())
                .addFilterAfter(dataVisibilityFilter, BasicAuthenticationFilter.class);
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth
                .userDetailsService(userDetailsService)
                .passwordEncoder(passwordEncoder())
            .and()
                .eraseCredentials(false);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    private RequestMatcher forPort(final int port) {
        return (HttpServletRequest request) -> port == request.getLocalPort();
    }
}
