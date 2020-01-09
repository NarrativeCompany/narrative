package org.narrative.network.core.security.jwt;

import org.narrative.network.shared.security.AccessViolation;
import org.narrative.network.shared.util.NetworkLogger;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Custom error handler.
 */
@Component
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private static final NetworkLogger logger = new NetworkLogger(RestAuthenticationEntryPoint.class);

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        // Unauthorized error to clients trying to access a protected resource.
        String message = "Responded to unauthorized access due to: " + authException.getMessage();
        logger.error(message);
        throw new AccessViolation(message);
    }
}
