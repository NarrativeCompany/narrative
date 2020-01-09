package org.narrative.network.customizations.narrative.service.api;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;

/**
 * Exception for validation errors - captures the validation context for error reporting.
 */
@Value
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class ValidationException extends RuntimeException {
    private final ValidationContext validationContext;

    public ValidationException(String message, ValidationContext validationContext) {
        super(message);
        this.validationContext = validationContext;
    }
}
