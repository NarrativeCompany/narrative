package org.narrative.network.core.security.jwt;

/**
 * Exception thrown when the JWT token is valid but the two factor authentication expiry in the token has expired.
 */
public class JwtToken2FAExpiredException extends JwtTokenInvalidException {
    public JwtToken2FAExpiredException(Throwable cause) {
        super(cause);
    }

    public JwtToken2FAExpiredException(String message) {
        super(message);
    }

    public JwtToken2FAExpiredException() {

    }
}
