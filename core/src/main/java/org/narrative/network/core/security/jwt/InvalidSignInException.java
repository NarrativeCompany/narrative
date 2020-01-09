package org.narrative.network.core.security.jwt;

import org.springframework.security.core.AuthenticationException;

/**
 * Generic exception thrown for invalid sign in issues, including invalid reCAPTCHA responses
 */
public class InvalidSignInException extends AuthenticationException {
    public InvalidSignInException(String message) {
        super(message);
    }
}
