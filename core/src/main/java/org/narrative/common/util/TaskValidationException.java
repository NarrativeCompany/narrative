package org.narrative.common.util;

import java.util.Collection;

/**
 * Created by IntelliJ IDEA.
 * User: jonmark
 * Date: 3/4/14
 * Time: 9:12 AM
 * <p>
 * JW: the only time that this exception should be thrown by the servlet is for the API, and we dont want those errors
 * to appear in the main errors list.  So I am going to make this into a ApplicationError.
 */
public class TaskValidationException extends ApplicationError {
    private final Collection<ValidationError> validationErrors;

    public TaskValidationException(Collection<ValidationError> validationErrors) {
        super(flattenErrorsForMessage(validationErrors));

        this.validationErrors = validationErrors;
    }

    private static String flattenErrorsForMessage(Collection<ValidationError> validationErrors) {
        StringBuilder sb = new StringBuilder();
        for (ValidationError error : validationErrors) {
            if (sb.length() > 0) {
                sb.append('\n');
            }
            sb.append(error.getMessage());
        }

        return sb.toString();
    }

    public Collection<ValidationError> getValidationErrors() {
        return validationErrors;
    }
}
