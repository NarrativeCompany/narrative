package org.narrative.network.customizations.narrative.service.impl.common;

import com.google.common.annotations.VisibleForTesting;
import lombok.NonNull;
import lombok.ToString;
import lombok.Value;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.internal.engine.messageinterpolation.InterpolationTermType;
import org.hibernate.validator.internal.engine.messageinterpolation.parser.TokenCollector;
import org.narrative.common.util.ValidationError;
import org.narrative.network.customizations.narrative.service.api.ValidationContext;
import org.springframework.context.MessageSource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Validation context for storing/retrieving validation errors.
 */
@ToString(onlyExplicitlyIncluded = true)
public class ValidationContextImpl implements ValidationContext {
    private final Object monitor = new Object();
    private final MessageSource messageSource;
    private final Locale locale;
    @ToString.Include
    private List<ValidationErrorWrapper> validationErrorList;

    private enum ValidationErrorType {
        FIELD,
        METHOD
    }

    @Value
    private static class ValidationErrorWrapper {
        private final ValidationErrorType errorType;
        private final ValidationError validationError;
    }

    public ValidationContextImpl(MessageSource messageSource, Locale locale) {
        this.messageSource = messageSource;
        this.locale = locale;
    }

    /**
     * Add a validation error to the list
     *
     * @param messageTemplate The message template to use
     * @param errorType The error type to use
     * @param bindValues Bind values to bind to the message (optional)
     */
    @VisibleForTesting
    void addValidationError(String fieldOrMethod, String messageTemplate, ValidationErrorType errorType, Object[] bindValues) {
        //Grab the message from the bundle and bind values (if present)
        String translatedMessage = messageSource.getMessage(messageTemplate, null, messageTemplate, locale);

        //Interpolate parameters if necessary
        String interpolatedMessage = ArrayUtils.isNotEmpty(bindValues) ? interpolateParameters(translatedMessage, bindValues) : translatedMessage;

        synchronized (monitor) {
            addValidationError(errorType, new ValidationError(interpolatedMessage, fieldOrMethod));
        }
    }

    private void addValidationError(ValidationErrorType errorType, ValidationError validationError) {
        if (validationErrorList == null) {
            validationErrorList = new ArrayList<>();
        }
        //Add to the list
        validationErrorList.add(new ValidationErrorWrapper(errorType, validationError));
    }

    /**
     * Interpolate parameters for a message.
     *
     * @param message    The unbound message
     * @param bindValues Values to bind to the message
     * @return The interpolated message
     */
    @VisibleForTesting
    String interpolateParameters(String message, Object[] bindValues) {
        AtomicInteger bindValIndex = new AtomicInteger(0);
        final Object[] bindableValues = bindValues == null ? new Object[0] : bindValues;
        TokenCollector tokenCollector = new TokenCollector(message, InterpolationTermType.PARAMETER);

        return tokenCollector.getTokenList().stream()
                .map(token -> {
                    if (token.isParameter() && bindValIndex.get() < bindableValues.length) {
                        return bindableValues[bindValIndex.getAndIncrement()].toString();
                    } else {
                        return token.getTokenValue();
                    }
                }).collect(Collectors.joining());
    }

    @Override
    public ValidationContext addFieldError(String fieldName, String messageTemplate) {
        addFieldError(fieldName, messageTemplate, ArrayUtils.EMPTY_STRING_ARRAY);
        return this;
    }

    @Override
    public ValidationContext addFieldError(String fieldName, String messageTemplate, String... bindValues) {
        addValidationError(fieldName, messageTemplate, ValidationErrorType.FIELD, bindValues);
        return this;
    }

    @Override
    public ValidationContext addRequiredFieldError(String fieldName) {
        this.addFieldError(fieldName, "field.required");
        return this;
    }

    @Override
    public ValidationContext addInvalidFieldError(String fieldName) {
        this.addFieldError(fieldName, "field.invalid");
        return this;
    }

    @Override
    public ValidationContext addMethodError(String messageTemplate) {
        addMethodError(messageTemplate, ArrayUtils.EMPTY_STRING_ARRAY);
        return this;
    }

    @Override
    public ValidationContext addMethodError(String messageTemplate, String... bindValues) {
        addValidationError(StringUtils.EMPTY, messageTemplate, ValidationErrorType.METHOD, bindValues);
        return this;
    }

    @Override
    public ValidationContext addValidationErrors(Collection<ValidationError> validationErrors) {
        synchronized (monitor) {
            for (ValidationError validationError : validationErrors) {
                ValidationErrorType errorType = isEmpty(validationError.getField()) ? ValidationErrorType.METHOD : ValidationErrorType.FIELD;
                addValidationError(errorType, validationError);
            }
        }
        return this;
    }

    @Override
    public boolean hasErrors() {
        synchronized (monitor) {
            return CollectionUtils.isNotEmpty(validationErrorList);
        }
    }

    /**
     * Get the list of filtered validation errors.
     *
     * @return {@link List} of {@link ValidationError}
     */
    public List<ValidationError> getValidationErrors(@NonNull EnumSet<ValidationErrorType> errorTypeSet) {
        synchronized (monitor) {
            return validationErrorList.stream()
                    .filter(wrapper -> errorTypeSet.contains(wrapper.errorType))
                    .map(wrapper -> wrapper.validationError)
                    .collect(Collectors.toList());
        }
    }

    @Override
    public List<ValidationError> getValidationErrors() {
        return getValidationErrors(EnumSet.allOf(ValidationErrorType.class));
    }

    @Override
    public List<ValidationError> getFieldValidationErrors() {
        return getValidationErrors(EnumSet.of(ValidationErrorType.FIELD));
    }

    @Override
    public List<ValidationError> getMethodValidationErrors() {
        return getValidationErrors(EnumSet.of(ValidationErrorType.METHOD));
    }
}
