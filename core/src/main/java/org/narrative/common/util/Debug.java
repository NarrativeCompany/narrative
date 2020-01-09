package org.narrative.common.util;

import javax.servlet.ServletException;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.el.ELException;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * [b]Features[/b]
 * The Debug class provides two main features:
 * [*] Assert statements (for those of us from the good ol' C++ days).
 * [*] Message Logging (to a file, database, System.out or any other
 * mechanism you define).
 * <p>
 * [b]Assert Mechanism[/b]
 * The assert mechanism works by checking a condition.  If the condition is
 * false, it throws a RuntimeException (actually an UnexpectedError which is
 * derived from RuntimeException).
 * <p>
 * The assert mechanism can be used as follows:
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * [code]
 * String readFile(String filename) {
 * Debug.assertMsg(logger, filename!=null, "Filename cannot be null);
 * try {
 * File f = new File(filename);
 * // do something with the file
 * } catch(Throwable t) {
 * // throw an assert with some context information
 * Debug.assertMsg(logger, false, "Failed getting " + filename, t);
 * }
 * }
 * [/code]
 * <p>
 * <p>
 * <p>
 * In the second assert statement the code catching the exception will have
 * access to the original exception thrown as well as the context of the exception.
 * <p>
 * [b]Disabling Debugging[/b]
 * The Debug class has a final static boolean (bDebug) that enables you to turn
 * the assert mechanism on or off.  If performance is a concern then a statement
 * like the following one will compile away to nothing (with most compilers) when
 * debugging is turned of.
 * [code]
 * if(Debug.bDebug) if(logger.isInfoEnabled()) logger.info( "Here is some information for you");
 * logger.error( "Whoops.  Something went wrong.");
 * [/code]
 * [b]Message Logging[/b]
 * The two statements above demonstrate the use of the logging mechanism.  You can
 * log a message and give it a flag specifying the type of message.  Debug supports filtering so
 * you it will check the flag (e.g. Debug.INFO) and match it against the current debugging
 * level.  If it does not match, nothing will be logged.  This makes it easy to log only errors
 * in a production environment and everything in a development environment (or only SQL
 * related messages, or only Servlet related messages, or only timing related messages, etc).
 * <p>
 * There are default logging mechanisms for File logging as well as System.out and
 * System.err.  You can implement the LogWriter interface to have your own custom logger,
 * e.g. one that writes to the database.  You may even want to implement a multithreaded
 * logging mechanism to improve speed.
 * <p>
 * [b]Miscellaneous[b]
 * Debug.stackTraceFromException(Throwable t) gets you a stack dump as a String for the
 * specified exception.
 * <p>
 * [b]Related Areas[/b]
 * IBM's developer works has a couple of logging packages, including a multi-threading one.
 * Javasoft appear to be working on an assert extension for Java.
 *
 * @author Peter Bryant (pbryant@bigfoot.com)
 */
public final class Debug {

    /**
     * get a string stack trace from the given exception
     */
    public static String stackTraceFromException(Throwable e) {
        // bl: ServletExceptions, ELExceptions, and JspExceptions are quite annoying.  they have a special
        // "rootCause" that isn't a standard Throwable cause.  thus, let's
        // look in our hierarchy of exceptions for a ServletException/ELException/JspException and
        // check if that ServletException/ELException/JspException has a rootCause.  if it does,
        // let's include that rootCause here.
        // bl: changed to handle by simply initializing the cause on the Throwable objects themselves.
        initCause(e);
        if (e == null) {
            return "";
        }
        StringWriter s = new StringWriter();
        PrintWriter p = new PrintWriter(s);
        e.printStackTrace(p);
        return s.toString();
    }

    /**
     * log or pop up a dialog if b is false;
     */
    public static boolean assertMsg(NarrativeLogger logger, boolean b, String msg, Throwable failedAssertMessage) {
        if (!b) {
            logger.error("Throwing assert: " + getErrorDetail(msg, failedAssertMessage));
            throw UnexpectedError.getRuntimeException(msg, failedAssertMessage);
            /*
            if(bDoAssertDlg)
                // do in the right thread!
                try {
                    Runnable r = new DoAssertDlg(failedAssertMessage.toString());
                    if(SwingUtilities.isEventDispatchThread())
                        r.run();
                    else SwingUtilities.invokeAndWait(r);
                } catch(Exception e) {}
            */
        }
        return b;
    }

    static public boolean assertMsg(NarrativeLogger logger, boolean b, Throwable failedAssertMessage) {
        if (!b) {
            return assertMsg(logger, b, null, failedAssertMessage);
        }
        return true;
    }

    /**
     * log or pop up a dialog (depending on setup) is b is fase
     */
    static public boolean assertMsg(NarrativeLogger logger, boolean b, String failedAssertMessage) {
        if (!b) {
            Throwable t = new Throwable(IPStringUtil.isEmpty(failedAssertMessage) ? "No error supplied" : failedAssertMessage);
            return assertMsg(logger, b, failedAssertMessage, t);
        }
        return true;
    }

    /**
     * Convenience method for creating asserts that check that values are not null.  Will output
     * the following "<field name> is a required field"
     *
     * @param val       The value to test.
     * @param fieldName The name of the field to output.
     */
    public static boolean assertNotNull(NarrativeLogger logger, Object val, String fieldName) {
        return assertMsg(logger, val != null, fieldName + " is a required field");
    }

    /**
     * return e's message as well as the err text (usually the context of the exception)
     */
    public static String getErrorDetail(String err, Throwable e) {
        if (e == null) {
            return err == null ? "" : err;
        }

        String msg = e.getMessage();
        msg = msg == null ? e.getClass().getName() : e.getClass().getName() + ": " + msg;
        err = err == null ? msg : err + "\nCaused by: '" + msg + '\'';
        return err;
    }

    public static String getErrorDetailIncludingCause(String err, Throwable t) {
        initCause(t);
        StringBuilder sb = new StringBuilder(getErrorDetail(err, t));
        Throwable cause = t.getCause();
        while (cause != null) {
            sb.append(getErrorDetail("", cause));
            cause = cause.getCause();
        }
        return sb.toString();
    }

    public static Throwable getCause(Throwable t) {
        if (t == null) {
            return null;
        }
        Throwable cause = t.getCause();
        if (cause != null) {
            return cause;
        }
        // for whatever reason, ServletException uses "rootCause" instead of the usual cause.
        if (t instanceof ServletException) {
            return ((ServletException) t).getRootCause();
        }
        // for whatever reason, ELException uses "rootCause" instead of the usual cause, too.
        if (t instanceof ELException) {
            return ((ELException) t).getRootCause();
        }
        // for whatever reason, JspException uses "rootCause" instead of the usual cause, too.
        if (t instanceof JspException) {
            return ((JspException) t).getRootCause();
        }
        // for whatever reason (probably a very old legacy JDBC reason), SQLException uses "nextException" instead of the usual cause, too.
        if (t instanceof SQLException) {
            return ((SQLException) t).getNextException();
        }

        return null;
    }

    public static Throwable getRootCause(Throwable t) {
        initCause(t);
        Throwable ret = t;
        while (ret != null) {
            Throwable cause = ret.getCause();
            if (cause == null) {
                return ret;
            }
            ret = cause;
        }
        assert false : "Failed identifying root cause!";
        return null;
    }

    public static void initCause(Throwable t) {
        initCause(t, new HashSet<Throwable>());
    }

    private static void initCause(Throwable t, Set<Throwable> throwables) {
        if (t == null) {
            return;
        }
        Throwable cause = t;
        while (true) {
            throwables.add(cause);
            Throwable newCause = cause.getCause();
            if (newCause == null) {
                break;
            }
            cause = newCause;
        }
        Throwable realCause = getCause(cause);
        // bl: make sure we don't create a loop in the exception chain, which would cause an infinite loop.
        if (realCause != null && !throwables.contains(realCause)) {
            cause.initCause(realCause);
            initCause(realCause, throwables);
        }
    }

    public static int getHashCodeFromException(Throwable t) {
        return Arrays.hashCode(Debug.getRootCause(t).getStackTrace());
    }

    public static int getRootCauseClassHashCode(Throwable t) {
        // bl: i noticed that the same exception class had different hash codes on different servlets.
        // changing so that instead we use the class name, which should provide a uniform hashcode
        // across servers.
        return Debug.getRootCause(t).getClass().getName().hashCode();
    }

}