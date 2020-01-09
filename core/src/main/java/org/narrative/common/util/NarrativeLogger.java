package org.narrative.common.util;

import ch.qos.logback.classic.Level;
import org.narrative.common.persistence.DAOObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Created by IntelliJ IDEA.
 * User: barry
 * Date: May 17, 2006
 * Time: 5:20:43 PM
 */
public class NarrativeLogger {

    private final Logger logger;

    /**
     * NarrativeLogger only supports loggers on a per class basis
     *
     * @param clazz to create logger from
     */
    public NarrativeLogger(Class clazz) {
        this.logger = LoggerFactory.getLogger(clazz.getName());
    }

    public void trace(String message) {
        doLog(Level.TRACE, message, null);
    }

    public void trace(String message, Object... objs) {
        if (objs != null) {
            message = objectsToMessage(message, objs);
        }
        trace(message);
    }

    public void trace(String message, Throwable t) {
        Debug.initCause(t);
        doLog(Level.TRACE, getMessageWithExceptionInfo(message, t), t);
    }

    public void trace(String message, Throwable t, Object... objs) {
        if (objs != null) {
            message = objectsToMessage(message, objs);
        }
        trace(message, t);
    }

    public void debug(String message) {
        doLog(Level.DEBUG, message, null);
    }

    public void debug(String message, Object... objs) {
        if (objs != null) {
            message = objectsToMessage(message, objs);
        }
        debug(message);
    }

    public void debug(String message, Throwable t) {
        Debug.initCause(t);
        doLog(Level.DEBUG, getMessageWithExceptionInfo(message, t), t);
    }

    public void debug(String message, Throwable t, Object... objs) {
        if (objs != null) {
            message = objectsToMessage(message, objs);
        }
        debug(message, t);
    }

    public void error(String message) {
        doLog(Level.ERROR, message, null);
    }

    public void error(String message, Object... objs) {
        if (objs != null) {
            message = objectsToMessage(message, objs);
        }
        error(message);
    }

    public void error(String message, Throwable t) {
        Debug.initCause(t);
        doLog(Level.ERROR, getMessageWithExceptionInfo(message, t), t);
    }

    public void error(String message, Throwable t, Object... objs) {
        if (objs != null) {
            message = objectsToMessage(message, objs);
        }
        error(message, t);
    }

    public void warn(String message) {
        doLog(Level.WARN, message, null);
    }

    public void warn(String message, Object... objs) {
        if (objs != null) {
            message = objectsToMessage(message, objs);
        }
        warn(message);
    }

    public void warn(String message, Throwable t) {
        Debug.initCause(t);
        doLog(Level.WARN, getMessageWithExceptionInfo(message, t), t);
    }

    public void warn(String message, Throwable t, Object... objs) {
        if (objs != null) {
            message = objectsToMessage(message, objs);
        }
        warn(message, t);
    }

    public void info(String message) {
        doLog(Level.INFO, message, null);
    }

    public void info(String message, Object... objs) {
        if (objs != null) {
            message = objectsToMessage(message, objs);
        }
        info(message);
    }

    public void info(String message, Throwable t) {
        Debug.initCause(t);
        doLog(Level.INFO, getMessageWithExceptionInfo(message, t), t);
    }

    public void info(String message, Throwable t, Object... objs) {
        if (objs != null) {
            message = objectsToMessage(message, objs);
        }
        info(message, t);
    }

    protected void doLog(final Level level, final String message, final Throwable t) {
        assert level != null : "Should never specify a logback Level that is null!";
        if (level == Level.TRACE) {
            logger.trace(message, t);
        } else if (level == Level.DEBUG) {
            logger.debug(message, t);
        } else if (level == Level.INFO) {
            logger.info(message, t);
        } else if (level == Level.WARN) {
            logger.warn(message, t);
        } else if (level == Level.ERROR) {
            logger.error(message, t);
        } else {
            assert false : "Found unsupported logback Log Level/" + level + " levelInt/" + level.toInt();
        }
    }

    public boolean isTraceEnabled() {
        return logger.isTraceEnabled();
    }

    public boolean isDebugEnabled() {
        return logger.isDebugEnabled();
    }

    public boolean isInfoEnabled() {
        return logger.isInfoEnabled();
    }

    public boolean isWarnEnabled() {
        return logger.isWarnEnabled();
    }

    public boolean isErrorEnabled() {
        return logger.isErrorEnabled();
    }

    private static String getMessageWithExceptionInfo(String message, Throwable t) {
        // bl: include the hashcode of the stack trace along with the exception class hashcode
        // in the log to make it easy to identify the same exceptions in error logs.
        StringBuilder sb = new StringBuilder(UnexpectedError.getErrorDetail(message, t));
        if (t instanceof ApplicationError) {
            ApplicationError ae = (ApplicationError) t;
            sb.append("\nAppError info: ");
            sb.append(ae.getSystemLogInfo());
        }
        sb.append("\nex:[");
        sb.append(Debug.getHashCodeFromException(t));
        sb.append("][");
        sb.append(Debug.getRootCauseClassHashCode(t));
        sb.append("]");
        return sb.toString();
    }

    protected static String objectsToMessage(String message, Object... objs) {
        if (isEmptyOrNull(objs)) {
            return message;
        }
        StringBuilder stringBuilder = new StringBuilder(message);
        for (Object obj : objs) {
            if (obj == null) {
                continue;
            }
            stringBuilder.append(IPUtil.getClassSimpleName(obj.getClass())).append(":");
            if (obj instanceof DAOObject) {
                DAOObject daoObj = (DAOObject) obj;
                Serializable id = daoObj.getOid();
                if (id != null) {
                    stringBuilder.append(id.toString());
                } else {
                    stringBuilder.append("null");
                }
            } else {
                stringBuilder.append(obj);
            }
            stringBuilder.append("/");
        }
        return stringBuilder.toString();
    }

    private static CurrentContextProvider currentContextProvider;

    public static String getDefaultCurrentContextInfo() {
        assert currentContextProvider != null : "Must specify a CurrentContextProvider before you can attempt to get the default current context info for logging purposes!";
        return currentContextProvider.getCurrentContext();
    }

    public static void setCurrentContextProvider(CurrentContextProvider currentContextProvider) {
        NarrativeLogger.currentContextProvider = currentContextProvider;
    }

    public static interface CurrentContextProvider {
        public String getCurrentContext();
    }
}
