package org.narrative.network.core.security.jwt;

/**
 * Date: 9/11/18
 * Time: 8:12 PM
 *
 * @author brian
 */
public class JwtTokenInvalidException extends RuntimeException {
    public JwtTokenInvalidException(Throwable cause) {
        super(cause);
    }

    public JwtTokenInvalidException(String message) {
        super(message);
    }

    public JwtTokenInvalidException() {

    }
}
