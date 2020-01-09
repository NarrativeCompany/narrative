package org.narrative.common.util;

import org.apache.commons.collections.CollectionUtils;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;

import java.util.Set;

/**
 * UnexpectedError beautifies any exception passed in to it for end user consumption.
 * <p>
 * permits chaining
 *
 * @author Peter Bryant(pbryant@bigfoot.com)
 */
public class UnexpectedError extends NarrativeException {

    private static final NarrativeLogger logger = new NarrativeLogger(UnexpectedError.class);

    private boolean isIgnorable;

    protected UnexpectedError(String error, Throwable original) {
        super(getErrorDetail(error, original), getCauseFromException(original));
        if (original != null && original instanceof UnexpectedError) {
            isIgnorable = ((UnexpectedError) original).isIgnorable;
        }
    }

    protected UnexpectedError(String error) {
        super(error);
    }

    protected UnexpectedError(String error, boolean log) {
        this(error);
        if (log && logger.isErrorEnabled()) {
            logger.error(error);
        }
    }

    protected UnexpectedError(String error, Throwable original, boolean log) {
        this(error, original);
        if (log && logger.isErrorEnabled()) {
            logger.error(error, original);
        }
    }

    public boolean isIgnorable() {
        return isIgnorable;
    }

    public static String getErrorDetail(String error, Throwable original) {
        if (original == null) {
            return error;
        }
        // if the original is already an UnexpectedError, then the cause will already be included in the message.
        // in that case, just stick on the new error to the beginning of the message without resolving the
        // full exception stack.
        if (original instanceof UnexpectedError) {
            return Debug.getErrorDetail(error, original);
        }

        String mess = Debug.getErrorDetailIncludingCause(error, original);
        if (original instanceof ConstraintViolationException) {
            ConstraintViolationException ise = (ConstraintViolationException) original;
            Set<ConstraintViolation<?>> violationSet = ise.getConstraintViolations();
            if (CollectionUtils.isNotEmpty(violationSet)) {
                StringBuilder ret = new StringBuilder(mess);
                for (ConstraintViolation violation : violationSet) {
                    ret.append("\n");
                    ret.append("Invalid value for: ");
                    ret.append(violation.getPropertyPath().toString());
                    ret.append(" value: ");
                    ret.append(violation.getInvalidValue());
                    ret.append(" message: ");
                    ret.append(violation.getMessage());
                }
                mess = ret.toString();
            }
        }
        return mess;
    }

    private static Throwable getCauseFromException(Throwable original) {
        if (original == null) {
            return null;
        }

        // don't nest UnexpectedErrors.  instead, pull out the original cause.
        if (original instanceof UnexpectedError) {
            if (original.getCause() != null) {
                return original.getCause();
            }

            // bl: i don't see any reason this is much of a problem.
            /*logger.error( "Nesting UnexpectedErrors!  Probably shouldn't be doing this.  You might want to investigate." +
                                 "\nOriginal: " + Debug.stackTraceFromException(original) +
                                 "\nNew: " + Debug.stackTraceFromException(new Throwable()));*/
        }

        // either the original was not an UnexpectedError or else it didn't have
        // a root cause.  in that case, just return the original.  this just means
        // that we will in fact allow for nested UnexpectedErrors.
        return original;
    }

    public static UnexpectedError getRuntimeException(String message) {
        return new UnexpectedError(message);
    }

    public static UnexpectedError getIgnorableRuntimeException(String message) {
        UnexpectedError ret = new UnexpectedError(message);
        ret.isIgnorable = true;
        return ret;
    }

    public static UnexpectedError getIgnorableRuntimeException(String message, Throwable t) {
        UnexpectedError ret = new UnexpectedError(message, t);
        ret.isIgnorable = true;
        return ret;
    }

    public static UnexpectedError getRuntimeException(String message, boolean log) {
        return new UnexpectedError(message, log);
    }

    public static NarrativeException getRuntimeException(String message, Throwable t) {
        return getRuntimeException(message, t, false);
    }

    public static NarrativeException getRuntimeException(String message, Throwable t, boolean log) {
        if (t instanceof ApplicationError) {
            ApplicationError ae = (ApplicationError) t;
            ae.appendSystemLogInfo(message);
            if (log && logger.isErrorEnabled()) {
                logger.error(message, t);
            }
            return ae;
        }
        // allow the ignorable flag to persist through "wrapped" UnexpectedErrors.
        UnexpectedError ret = new UnexpectedError(message, t, log);
        if (t instanceof UnexpectedError && ((UnexpectedError) t).isIgnorable) {
            ret.isIgnorable = true;
        }
        return ret;
    }
}
