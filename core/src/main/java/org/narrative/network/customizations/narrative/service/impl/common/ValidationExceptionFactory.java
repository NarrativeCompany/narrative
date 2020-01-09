package org.narrative.network.customizations.narrative.service.impl.common;

import org.narrative.network.customizations.narrative.service.api.ValidationContext;
import org.narrative.network.customizations.narrative.service.api.ValidationException;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.stereotype.Component;

/**
 * Factory for building validation exceptions with a one-liner.
 */
@Component
public class ValidationExceptionFactory {
    private ObjectFactory<ValidationContext> validationContextObjectFactory;

    public ValidationExceptionFactory(ObjectFactory<ValidationContext> validationContextObjectFactory) {
        this.validationContextObjectFactory = validationContextObjectFactory;
    }

    public ValidationException forSingleField(String exMessage, String fieldName, String messageTemplate) {
        return buildException(exMessage,
                validationContextObjectFactory.getObject()
                        .addFieldError(fieldName, messageTemplate, ArrayUtils.EMPTY_STRING_ARRAY)
        );
    }

    public ValidationException forFieldError(String exMessage, String fieldName, String messageTemplate, String... bindValues) {
        return buildException(exMessage, validationContextObjectFactory.getObject()
                .addFieldError(fieldName, messageTemplate, bindValues)
        );
    }

    public ValidationException forRequiredFieldError(String exMessage, String fieldName) {
        return buildException(exMessage,
         validationContextObjectFactory.getObject()
            .addRequiredFieldError(fieldName)
        );
    }

    public ValidationException forInvalidFieldError(String exMessage, String fieldName) {
        return buildException(exMessage,
            validationContextObjectFactory.getObject()
                    .addInvalidFieldError(fieldName)
        );
    }

    public ValidationException forInvalidFieldErrors(String exMessage, String...fieldNames) {
        ValidationContext validationContext = validationContextObjectFactory.getObject();
        for (String fieldName: fieldNames) {
            validationContext.addInvalidFieldError(fieldName);
        }
        return buildException(exMessage, validationContext);
    }

    public ValidationException forMethodError(String exMessage, String messageTemplate) {
        return buildException(exMessage,
            validationContextObjectFactory.getObject()
                    .addMethodError(messageTemplate, ArrayUtils.EMPTY_STRING_ARRAY)
        );
    }

    public ValidationException forMethodError(String exMessage, String messageTemplate, String... bindValues) {
        return buildException(exMessage,
                validationContextObjectFactory.getObject()
                        .addMethodError(messageTemplate, bindValues)
        );
    }

    private ValidationException buildException(String exMessage, ValidationContext validationContext) {
        return new ValidationException(exMessage, validationContext);
    }
}
