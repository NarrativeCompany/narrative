package org.narrative.network.core.statistics;

import org.narrative.common.persistence.OID;
import org.narrative.common.util.ApplicationError;
import org.narrative.common.util.Debug;
import org.narrative.common.util.LRUMap;
import org.narrative.common.util.TaskValidationException;
import org.narrative.common.util.UnexpectedError;
import org.narrative.common.web.RequestResponseHandler;
import org.narrative.network.core.security.jwt.JwtTokenInvalidException;
import org.narrative.network.customizations.narrative.service.api.ValidationException;
import org.narrative.network.shared.context.NetworkContext;
import org.narrative.network.shared.context.NetworkContextImplBase;
import org.narrative.network.shared.security.PrimaryRole;
import org.narrative.network.shared.util.NetworkLogger;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.firewall.RequestRejectedException;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Created by IntelliJ IDEA.
 * User: Paul
 * Date: Mar 13, 2006
 * Time: 11:02:51 PM
 */
public class StatisticManager {

    private static final LRUMap<Integer, ExceptionInfo> MOST_COMMON_EXCEPTIONS = new LRUMap<>(500);
    private static final LRUMap<Integer, ExceptionInfo> MOST_COMMON_APPLICATION_ERRORS = new LRUMap<>(500);
    private static final LRUMap<Integer, AjaxErrorInfo> MOST_COMMON_AJAX_ERRORS = new LRUMap<>(500);

    /**
     * the list of clas
     */
    private static final List<Class<? extends Exception>> IGNORABLE_EXCEPTION_CLASSES = Collections.unmodifiableList(Arrays.asList(ApplicationError.class, TaskValidationException.class, ValidationException.class, UsernameNotFoundException.class, MethodArgumentNotValidException.class, BadCredentialsException.class, JwtTokenInvalidException.class, RequestRejectedException.class));

    public static void recordException(Throwable t, boolean hasExceptionBubbledToFilter, RequestResponseHandler reqRespFromNetworkFilter) {
        int hashcode = Debug.getHashCodeFromException(t);
        LRUMap<Integer, ExceptionInfo> map;

        if (isIgnorableException(t) || isIgnorableException(Debug.getRootCause(t))) {
            map = MOST_COMMON_APPLICATION_ERRORS;
        } else {
            map = MOST_COMMON_EXCEPTIONS;
        }

        ExceptionInfo exceptionInfo = map.get(hashcode);
        if (exceptionInfo == null) {
            exceptionInfo = new ExceptionInfo(t);
        } else {
            exceptionInfo.addCount();
        }
        StringBuilder extraLogInfo = new StringBuilder(Thread.currentThread().getName());
        extraLogInfo.append(" ");
        extraLogInfo.append(new Timestamp(System.currentTimeMillis()).toString());
        extraLogInfo.append(" ");
        extraLogInfo.append(NetworkLogger.getCurrentContextInfo(reqRespFromNetworkFilter, true));
        if (t instanceof ApplicationError) {
            ApplicationError ae = (ApplicationError) t;
            extraLogInfo.append("\nAppError info: ");
            extraLogInfo.append(ae.getSystemLogInfo());
        }
        extraLogInfo.append(" Exception messages: ");
        extraLogInfo.append(UnexpectedError.getErrorDetail("", t));
        exceptionInfo.addExtraLogInfo(extraLogInfo.toString());
        if (NetworkContextImplBase.isNetworkContextSet()) {
            NetworkContext networkContext = NetworkContextImplBase.current();
            if (networkContext.isHasPrimaryRole()) {
                PrimaryRole primaryRole = networkContext.getPrimaryRole();
                if (exists(primaryRole) && primaryRole.isSpider()) {
                    exceptionInfo.addSpiderCount();
                }
            }
        }
        if (hasExceptionBubbledToFilter) {
            exceptionInfo.addBubbledToFilterCount();
        }
        // bl: always do a put so that the most common errors stay in the map
        map.put(hashcode, exceptionInfo);
    }

    private static boolean isIgnorableException(Throwable t) {
        for (Class<? extends Exception> ignorableExceptionClass : IGNORABLE_EXCEPTION_CLASSES) {
            if(ignorableExceptionClass.isAssignableFrom(t.getClass())) {
                return true;
            }
        }
        // bl: treat "ignorable" UnexpectedErrors just as if they were ApplicationErrors
        if(t instanceof UnexpectedError && ((UnexpectedError) t).isIgnorable()) {
            return true;
        }
        return false;
    }

    public static List<ExceptionInfo> getMostCommonExceptions() {
        return new ArrayList<>(MOST_COMMON_EXCEPTIONS.getMap().values());
    }

    public static void removeException(int hashcode) {
        MOST_COMMON_EXCEPTIONS.remove(hashcode);
    }

    public static void clearExceptions() {
        MOST_COMMON_EXCEPTIONS.clear();
    }

    public static List<ExceptionInfo> getMostCommonApplicationErrors() {
        return new ArrayList<>(MOST_COMMON_APPLICATION_ERRORS.getMap().values());
    }

    public static void removeApplicationError(int hashcode) {
        MOST_COMMON_APPLICATION_ERRORS.remove(hashcode);
    }

    public static void clearApplicationErrors() {
        MOST_COMMON_APPLICATION_ERRORS.clear();
    }

    public static void recordAjaxError(PrimaryRole primaryRole, OID visitOid, String referenceId, String error, String stackTrace, String debugHtml) {
        int hashcode = stackTrace.hashCode();

        AjaxErrorInfo ajaxErrorInfo = MOST_COMMON_AJAX_ERRORS.get(hashcode);
        if (ajaxErrorInfo == null) {
            synchronized (StatisticManager.class) {
                ajaxErrorInfo = MOST_COMMON_AJAX_ERRORS.get(hashcode);
                if (ajaxErrorInfo == null) {
                    MOST_COMMON_AJAX_ERRORS.put(hashcode, ajaxErrorInfo = new AjaxErrorInfo(error, stackTrace));
                }
            }
        }
        ajaxErrorInfo.addReferenceIdInfo(primaryRole, visitOid, new Timestamp(System.currentTimeMillis()), referenceId, debugHtml);
        // bl: always do a put so that the most common errors stay in the map
        MOST_COMMON_AJAX_ERRORS.put(hashcode, ajaxErrorInfo);
    }

    public static List<AjaxErrorInfo> getMostCommonAjaxErrors() {
        return new ArrayList<>(MOST_COMMON_AJAX_ERRORS.getMap().values());
    }

    public static void removeAjaxError(int hashcode) {
        MOST_COMMON_AJAX_ERRORS.remove(hashcode);
    }

    public static void clearAjaxErrors() {
        MOST_COMMON_AJAX_ERRORS.clear();
    }

}
