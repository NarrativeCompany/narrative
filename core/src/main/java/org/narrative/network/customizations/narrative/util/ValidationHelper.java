package org.narrative.network.customizations.narrative.util;

import org.narrative.network.customizations.narrative.service.api.ValidationContext;
import org.narrative.network.customizations.narrative.service.api.ValidationException;
import io.jsonwebtoken.lang.Collections;
import org.springframework.beans.factory.ObjectFactory;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import java.util.Set;

/**
 * A helper to validate objects via Java validation API.
 */
public class ValidationHelper {
    private final ObjectFactory<ValidationContext> validationContextObjectFactory;
    private final Validator validator;

    public ValidationHelper(ObjectFactory<ValidationContext> validationContextObjectFactory, Validator validator) {
        this.validationContextObjectFactory = validationContextObjectFactory;
        this.validator = validator;
    }

    /**
     * Validate an object annotated with Java validation API annotations.
     *
     * @param object The object to validate
     * @return A {@link ValidationContext} if validation errors were found, null otherwise.
     */
    public <T> ValidationContext validateObject(T object) {
        ValidationContext validationContext = null;
        Set<ConstraintViolation<T>> violationSet = validator.validate(object);
        if (!Collections.isEmpty(violationSet)) {
            validationContext = validationContextObjectFactory.getObject();
            for (ConstraintViolation<T> cv : violationSet) {
                validationContext.addFieldError(cv.getPropertyPath().toString(), cv.getMessage());
            }
        }
        return validationContext;
    }

    /**
     * Validate an object annotated with Java validation API annotations.
     *
     * @param object           The object to validate
     * @param exceptionMessage Message to use for exception on validation failure.
     * @throws ValidationException on validation errors
     */
    public <T> void validateObject(T object, String exceptionMessage) {
        ValidationContext ctx = validateObject(object);
        if (ctx != null) {
            throw new ValidationException(exceptionMessage, ctx);
        }
    }

    /**
     * Build an empty validation context
     * @return A shiny new ValidationContext
     */
    public ValidationContext buildValidationContext() {
        return validationContextObjectFactory.getObject();
    }
}
