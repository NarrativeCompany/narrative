package org.narrative.network.core.cluster.actions.server;

import org.narrative.network.core.statistics.ErrorInfo;
import org.narrative.network.core.statistics.StatisticManager;

import java.util.List;
import java.util.Map;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Created by IntelliJ IDEA.
 * User: barry
 * Date: Dec 13, 2005
 * Time: 11:49:45 AM
 */
public class ErrorStatusAction extends SystemMonitoringAction {

    private Map<ErrorType, List<? extends ErrorInfo>> errorTypeToErrors;

    public String input() throws Exception {
        errorTypeToErrors = newLinkedHashMap();
        errorTypeToErrors.put(ErrorType.EXCEPTION, StatisticManager.getMostCommonExceptions());
        errorTypeToErrors.put(ErrorType.AJAX_ERROR, StatisticManager.getMostCommonAjaxErrors());
        errorTypeToErrors.put(ErrorType.APPLICATION_ERROR, StatisticManager.getMostCommonApplicationErrors());
        return INPUT;
    }

    public Map<ErrorType, List<? extends ErrorInfo>> getErrorTypeToErrors() {
        return errorTypeToErrors;
    }

    public static enum ErrorType {
        EXCEPTION,
        APPLICATION_ERROR,
        AJAX_ERROR;

        public String getTitle() {
            switch (this) {
                case EXCEPTION:
                    return "Errors";
                case APPLICATION_ERROR:
                    return "Application Errors";
                default:
                    assert isAjaxError() : "Found an unsupported ErrorType/" + this;
                    return "AJAX Errors";
            }
        }

        public boolean isException() {
            return this == EXCEPTION;
        }

        public boolean isApplicationError() {
            return this == APPLICATION_ERROR;
        }

        public boolean isAjaxError() {
            return this == AJAX_ERROR;
        }
    }
}
