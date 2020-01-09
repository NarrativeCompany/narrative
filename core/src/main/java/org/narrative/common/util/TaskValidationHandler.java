package org.narrative.common.util;

import java.util.LinkedList;
import java.util.List;

import static org.narrative.network.shared.util.NetworkCoreUtils.*;

/**
 * Created by IntelliJ IDEA.
 * User: Paul
 * Date: Sep 20, 2007
 * Time: 11:44:32 AM
 * To change this template use File | Settings | File Templates.
 */
public class TaskValidationHandler extends BaseValidationHandler {

    private final List<ValidationError> validationErrors = new LinkedList<ValidationError>();

    public String getText(String wordlet, Object... args) {
        return wordlet(wordlet, args);
    }

    public void addWordletizedActionError(String anErrorMessage, Object... args) {
        validationErrors.add(new ValidationError(getText(anErrorMessage, args)));
    }

    public void addWordletizedFieldError(String fieldName, String errorMessage, Object... args) {
        validationErrors.add(new ValidationError(getText(errorMessage, args), fieldName));
    }

    @Override
    public void addActionError(String anErrorMessage) {
        validationErrors.add(new ValidationError(anErrorMessage));
    }

    @Override
    public void addFieldError(String fieldName, String errorMessage) {
        validationErrors.add(new ValidationError(errorMessage, fieldName));
    }

    public List<ValidationError> getValidationErrors() {
        return validationErrors;
    }

    public boolean hasErrors() {
        return !validationErrors.isEmpty();
    }

    public boolean isThrowApplicationErrorOnValidationError() {
        return true;
    }
}
