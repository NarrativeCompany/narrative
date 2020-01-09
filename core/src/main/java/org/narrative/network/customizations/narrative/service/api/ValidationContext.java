package org.narrative.network.customizations.narrative.service.api;

import org.narrative.common.util.ValidationError;

import java.util.Collection;
import java.util.List;

/**
 * Validation context for storing/retrieving validation errors.
 */
public interface ValidationContext {
    /**
     * Add a field error to the context.
     *
     * @param fieldName       The name of the field
     * @param messageTemplate The template to use
     */
    ValidationContext addFieldError(String fieldName, String messageTemplate);

    /**
     * Add a field error to the context.
     *
     * @param fieldName       The name of the field
     * @param messageTemplate The template to use
     * @param bindValues      Values to bind to the template
     */
    ValidationContext addFieldError(String fieldName, String messageTemplate, String... bindValues);

    /**
     * Add a required field error to the context.
     *
     * @param fieldName  The name of the field
     */
    ValidationContext addRequiredFieldError(String fieldName);

    /**
     * Add an invalid field error to the context.
     *
     * @param fieldName  The name of the field
     */
    ValidationContext addInvalidFieldError(String fieldName);

    /**
     * Add a method error to the context.
     *
     * @param messageTemplate The template to use
     */
    ValidationContext addMethodError(String messageTemplate);

    /**
     * Add a method error to the context.
     *
     * @param messageTemplate The template to use
     * @param bindValues      The bind values for the template
     */
    ValidationContext addMethodError(String messageTemplate, String... bindValues);

    /**
     * Bulk add validation errors to the context.
     *
     * @param validationErrors The errors to add
     */
    ValidationContext addValidationErrors(Collection<ValidationError>  validationErrors);

    /**
     * Does this context contain errors?
     *
     * @return true if errors are present in the context
     */
    boolean hasErrors();

    /**
     * Get the list of all validation errors.
     *
     * @return {@link List} of {@link ValidationError}
     */
    List<ValidationError> getValidationErrors();

    /**
     * Get the list of field validation errors.
     *
     * @return {@link List} of {@link ValidationError} for field validation errors
     */
    List<ValidationError> getFieldValidationErrors();

    /**
     * Get the list of method validation errors.
     *
     * @return {@link List} of {@link ValidationError} for method validation errors
     */
    List<ValidationError> getMethodValidationErrors();
}
