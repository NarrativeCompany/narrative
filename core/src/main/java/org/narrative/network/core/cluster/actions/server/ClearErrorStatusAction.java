package org.narrative.network.core.cluster.actions.server;

import org.narrative.common.util.UnexpectedError;
import org.narrative.common.web.HttpRequestType;
import org.narrative.common.web.struts.MethodDetails;
import org.narrative.network.core.statistics.StatisticManager;
import org.narrative.network.shared.struts.NetworkResponses;

/**
 * Date: 8/2/13
 * Time: 2:21 PM
 * User: jonmark
 */
public class ClearErrorStatusAction extends SystemMonitoringAction {
    public static final String ACTION_NAME = "clear-error";
    public static final String FULL_ACTION_PATH = "/" + ACTION_NAME;

    private ErrorStatusAction.ErrorType errorType;
    private Integer stackTraceHashcode;
    private boolean clearAll;

    public static final String ERROR_TYPE_PARAM = "errorType";
    public static final String STACK_TRACE_HASHCODE_PARAM = "stackTraceHashcode";
    public static final String CLEAR_ALL_PARAM = "clearAll";

    @Override
    @MethodDetails(requestType = HttpRequestType.AJAX)
    public String execute() throws Exception {
        if (errorType == null) {
            throw UnexpectedError.getRuntimeException("Must supply errorType to clear error status for!");
        }
        if (stackTraceHashcode != null) {
            if (errorType.isApplicationError()) {
                StatisticManager.removeApplicationError(stackTraceHashcode);
            } else if (errorType.isAjaxError()) {
                StatisticManager.removeAjaxError(stackTraceHashcode);
            } else {
                StatisticManager.removeException(stackTraceHashcode);
            }

        } else if (clearAll) {
            if (errorType.isApplicationError()) {
                StatisticManager.clearApplicationErrors();
            } else if (errorType.isAjaxError()) {
                StatisticManager.clearAjaxErrors();
            } else {
                StatisticManager.clearExceptions();
            }
        }

        return NetworkResponses.emptyResponse();
    }

    public void setErrorType(ErrorStatusAction.ErrorType errorType) {
        this.errorType = errorType;
    }

    public void setStackTraceHashcode(Integer stackTraceHashcode) {
        this.stackTraceHashcode = stackTraceHashcode;
    }

    public void setClearAll(boolean clearAll) {
        this.clearAll = clearAll;
    }
}
