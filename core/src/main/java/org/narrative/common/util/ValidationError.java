package org.narrative.common.util;

/**
 * Created by IntelliJ IDEA.
 * User: Paul
 * Date: Sep 20, 2007
 * Time: 2:47:12 PM
 * To change this template use File | Settings | File Templates.
 */
public class ValidationError {
    private String message;
    private String field;

    public ValidationError(String message) {
        this(message, null);
    }

    public ValidationError(String message, String field) {
        this.message = message;
        this.field = field;
    }

    public String getMessage() {
        return message;
    }

    public String getField() {
        return field;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (field != null) {
            sb.append(field).append(": ");
        }
        sb.append(" ");
        sb.append(message);

        return sb.toString();
    }
}
