package org.narrative.network.core.security.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.narrative.base.LogSuppressor;
import org.narrative.config.properties.NarrativeProperties;
import org.narrative.network.core.security.NarrativeUserDetailsServiceImpl;
import org.narrative.network.customizations.narrative.service.impl.user.UserServiceImpl;
import org.narrative.network.shared.context.NetworkContextImplBase;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Mocked;
import mockit.Tested;
import mockit.Verifications;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.HandlerExceptionResolver;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;

class JwtAuthenticationTokenFilterTest {

    @Tested
    private JwtAuthenticationTokenFilter jwtAuthenticationTokenFilter;

    @Injectable
    private JwtUtil jwtUtil;

    @Injectable
    private NarrativeProperties narrativeProperties;

    @Injectable
    private NarrativeUserDetailsServiceImpl userDetailsService;

    @Injectable
    private UserServiceImpl userService;

    @Injectable
    private NetworkContextImplBase networkContextImplBase;

    @Injectable
    private HandlerExceptionResolver resolver;

    private ObjectMapper objectMapper = new ObjectMapper();

    private final LogSuppressor logSuppressor = new LogSuppressor();

    @BeforeEach
    public void init() {
        logSuppressor.suppressLogs(JwtAuthenticationTokenFilter.class);
    }

    @AfterEach
    public void tearDown() {
        logSuppressor.resumeLogs(JwtAuthenticationTokenFilter.class);
    }

    @Test
    void doFilterInternal_nullParsedTokens(@Mocked HttpServletRequest request, @Mocked HttpServletResponse response, @Mocked FilterChain filterChain) throws ServletException, IOException {
        String header = "Bearer ";

        new Expectations() {{
            jwtUtil.extractJwtStringFromRequest((HttpServletRequest) any); result = "someJWT";
            jwtUtil.parseAndValidateJWTFromString(anyString); result = new JwtTokenInvalidException();
        }};

        jwtAuthenticationTokenFilter.doFilterInternal(request, response, filterChain);

        new Verifications(){{
            resolver.resolveException(request, response, withNull(), (JwtTokenInvalidException) any);
        }};
    }


}