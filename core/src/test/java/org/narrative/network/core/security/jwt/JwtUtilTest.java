package org.narrative.network.core.security.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.narrative.base.LogSuppressor;
import org.narrative.config.properties.NarrativeProperties;
import org.narrative.network.core.security.NarrativeUserDetailsServiceImpl;
import org.narrative.network.customizations.narrative.service.impl.user.UserServiceImpl;
import org.narrative.network.shared.context.NetworkContextImplBase;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwsHeader;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.lang.Assert;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.web.servlet.HandlerExceptionResolver;

import javax.servlet.ServletException;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class JwtUtilTest {
    @Tested
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
    private MessageSourceAccessor messages;

    @Injectable
    private HandlerExceptionResolver resolver;

    @Injectable
    private ObjectMapper objectMapper = new ObjectMapper();

    private final LogSuppressor logSuppressor = new LogSuppressor();

    @BeforeEach
    public void init() {
        logSuppressor.suppressLogs(JwtUtil.class);
    }

    @AfterEach
    public void tearDown() {
        logSuppressor.resumeLogs(JwtUtil.class);
    }

    @Test
    void parseAuthToken_signatureException() throws ServletException {
        new Expectations() {{
            narrativeProperties.getSecurity().getJwtSecretBytes();
            result = new SignatureException("");
        }};
        Jws<Claims> result = jwtUtil.parseAndValidateJwtToken("someToken");
        Assert.isNull(result);
    }

    @Test
    void parseAuthToken_malformedException() throws ServletException {
        new Expectations() {{
            narrativeProperties.getSecurity().getJwtSecretBytes();
            result = new MalformedJwtException("");
        }};
        Jws<Claims> result = jwtUtil.parseAndValidateJwtToken("someToken");
        Assert.isNull(result);
    }

    @Test
    void parseAuthToken_expiredException() throws ServletException {
        new Expectations() {{
            narrativeProperties.getSecurity().getJwtSecretBytes();
            result = new ExpiredJwtException((JwsHeader) any, (Claims) any, anyString);
        }};
        assertThrows(JwtTokenInvalidException.class, () -> {
            jwtUtil.parseAndValidateJwtToken("someToken");
        });
    }

    @Test
    void parseAuthToken_unsupportedException() throws ServletException {
        new Expectations() {{
            narrativeProperties.getSecurity().getJwtSecretBytes();
            result = new UnsupportedJwtException("");
        }};
        Jws<Claims> result = jwtUtil.parseAndValidateJwtToken("someToken");
        Assert.isNull(result);
    }

    @Test
    void parseAuthToken_illegalArgException() throws ServletException {
        new Expectations() {{
            narrativeProperties.getSecurity().getJwtSecretBytes();
            result = new IllegalArgumentException("");
        }};
        Jws<Claims> result = jwtUtil.parseAndValidateJwtToken("someToken");
        Assert.isNull(result);
    }
}
