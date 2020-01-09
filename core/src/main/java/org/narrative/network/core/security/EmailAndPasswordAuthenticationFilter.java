package org.narrative.network.core.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.narrative.common.util.IPDateUtil;
import org.narrative.config.properties.NarrativeProperties;
import org.narrative.network.core.security.jwt.InvalidSignInException;
import org.narrative.network.core.security.jwt.JwtUtil;
import org.narrative.network.core.user.User;
import org.narrative.network.core.user.services.RecaptchaValidation;
import org.narrative.network.customizations.narrative.controller.postbody.user.LoginInputDTO;
import org.narrative.network.customizations.narrative.service.impl.StaticMethodWrapper;
import org.narrative.network.shared.util.NetworkLogger;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.servlet.HandlerExceptionResolver;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static org.narrative.network.core.security.jwt.JwtUtil.*;
import static org.narrative.network.shared.util.NetworkCoreUtils.*;

/**
 * Custom {@link UsernamePasswordAuthenticationFilter} that uses a provided email/password for authentication
 */
public class EmailAndPasswordAuthenticationFilter extends AbstractAuthenticationProcessingFilter {
    private static final NetworkLogger logger = new NetworkLogger(EmailAndPasswordAuthenticationFilter.class);

    private final AuthenticationManager authenticationManager;
    private final NarrativeProperties narrativeProperties;
    private final HandlerExceptionResolver resolver;
    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;
    private final StaticMethodWrapper staticMethodWrapper;

    public EmailAndPasswordAuthenticationFilter(AuthenticationManager authenticationManager,
                                                NarrativeProperties narrativeProperties,
                                                HandlerExceptionResolver resolver,
                                                JwtUtil jwtUtil,
                                                MessageSourceAccessor messageSourceAccessor,
                                                ObjectMapper objectMapper,
                                                StaticMethodWrapper staticMethodWrapper) {
        super(new AntPathRequestMatcher(narrativeProperties.getSecurity().getLoginURI(), HttpMethod.POST.name()));

        this.authenticationManager = authenticationManager;
        this.narrativeProperties = narrativeProperties;
        this.resolver = resolver;
        this.jwtUtil = jwtUtil;
        this.messages = messageSourceAccessor;
        this.objectMapper = objectMapper;
        this.staticMethodWrapper = staticMethodWrapper;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) {

        // 1. Get credentials from request JSON
        LoginInputDTO loginInput = null;
        if (MediaType.APPLICATION_JSON_VALUE.equals(request.getHeader(HttpHeaders.CONTENT_TYPE))) {
            try {
                /*
                 * HttpServletRequest can be read only once
                 */
                String json = IOUtils.toString(request.getReader());

                //json transformation
                loginInput = objectMapper.readValue(json, LoginInputDTO.class);

                // set the request body object on the RequestResponseHandler for logging purposes.
                staticMethodWrapper.networkContext().getReqResp().setRequestBodyObject(loginInput);
            } catch (Exception e) {
                // log & ignore the exception so we fall through and throw BadCredentialsException below.
                if(logger.isWarnEnabled()) logger.warn("Failed processing JSON login input", e);
            }
        }

        if (loginInput==null || StringUtils.isEmpty(loginInput.getEmailAddress()) || StringUtils.isEmpty(loginInput.getPassword())) {
            throw new BadCredentialsException("Empty email address or password supplied");
        }

        // bl: make sure that the reCAPTCHA response is valid before we do any further processing
        if(!RecaptchaValidation.validate(loginInput.getRecaptchaResponse(), staticMethodWrapper.networkContext())) {
            throw new InvalidSignInException(wordlet("reCaptcha.error"));
        }

        // 2. Create auth object with credentials. This will be used by AuthenticationManager
        NarrativePreAuthenticationToken authenticationToken = NarrativePreAuthenticationToken.builder()
                .emailAddress(loginInput.getEmailAddress())
                .password(loginInput.getPassword())
                .rememberMe(loginInput.isRememberMe())
                .build();

        // 3. Authentication authenticates the user. Then load the user.
        return authenticationManager.authenticate(authenticationToken);
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) {

        NarrativeUserDetails userDetails = (NarrativeUserDetails) authResult.getPrincipal();
        String jwtSubject = jwtUtil.getJwtSubject(userDetails.getEmailAddress(), userDetails.getPassword());

        long now = System.currentTimeMillis();

        Map<String, Object> claimMap = new HashMap<>();

        User user = User.dao().get(userDetails.getUserOID());

        //Handle augmentation of the JWT claims for 2FA enabled users
        boolean jwt2FAExpired = false;
        if (userDetails.isTwoFactorAuthenticationEnabled()) {
            //If 2FA is enabled for this user, set the token 2FA expiry to some time in the past to force the client
            // to "upgrade" its token to a valid expiry immediately after login by POSTing a two factor code
            jwt2FAExpired = true;
            Instant jwt2FAExpiryInstant = Instant.now().minus(1, ChronoUnit.DAYS);

            //Add a claim to the JWT for the two factor expiry
            claimMap.put(JwtUtil.TWO_FACTOR_AUTH_EXPIRY_CLAIM, Date.from(jwt2FAExpiryInstant));

            //Hash the 2FA secret and add as a claim so we can verify that the secret in the token matches the user's current secret
            String hashedPassword = BCrypt.hashpw(user.getTwoFactorAuthenticationSecretKey(), BCrypt.gensalt());
            claimMap.put(JwtUtil.TWO_FACTOR_AUTH_SECRET_HASH_CLAIM, hashedPassword);
        }

        //Add the authorities to the claim map
        claimMap.put(AUTHORITIES_CLAIM, authResult.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList()));

        NarrativeLoginDetails narrativeLoginDetails = (NarrativeLoginDetails)authResult.getDetails();

        String token = jwtUtil.buildJWTToken(
                //Use the user OID as the Id
                userDetails.getUserOID().toString(),
                //Subject
                jwtSubject,
                //Issued at date
                new Date(now),
                //Expiration date
                getJwtExpiration(request, narrativeLoginDetails.isRememberMe(), now),
                //Additional claims
                claimMap
        );

        if (logger.isDebugEnabled()) {
            logger.debug("Generated JWT String: " + token);
        }

        //Write the token to the response
        jwtUtil.writeJWTStringAsJSONToResponse(token, response, jwt2FAExpired);

        //The request is over.  No need to continue chaining or to set SecurityContextHolder
    }

    @NotNull
    private Date getJwtExpiration(HttpServletRequest request, boolean rememberMe, long now) {
        Date jwtExpiration;
        if (rememberMe) {
            jwtExpiration = new Date(now + IPDateUtil.YEAR_IN_MS);
        } else {
            jwtExpiration = new Date(now + narrativeProperties.getSecurity().getExpiration().toMillis());
        }
        return jwtExpiration;
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException {
        // bl: pass through the exception to our exception handler to deal with
        resolver.resolveException(request, response, null, failed);
    }

    public void setMessageSourceAccessor(MessageSourceAccessor messageSourceAccessor) {
        messages = messageSourceAccessor;
    }
}
