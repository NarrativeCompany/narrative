package org.narrative.common.util;

/**
 * Date: Oct 16, 2007
 * Time: 8:29:50 AM
 *
 * @author brian
 */
public class NarrativeException extends RuntimeException {
    public NarrativeException() {
    }

    public NarrativeException(String message) {
        super(message);
    }

    public NarrativeException(String message, Throwable cause) {
        super(message, cause);
    }

    public NarrativeException(Throwable cause) {
        super(cause);
    }
}
