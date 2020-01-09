package org.narrative.network.core.cluster.actions.server;

import org.narrative.common.core.services.interceptors.BypassHtmlDisable;
import org.narrative.common.web.HttpRequestType;
import org.narrative.common.web.struts.MethodDetails;
import org.narrative.common.web.struts.ReadOnly;
import org.narrative.network.core.statistics.StatisticManager;
import org.narrative.network.shared.authentication.UserSession;
import org.narrative.network.shared.baseactions.NetworkAction;
import org.narrative.network.shared.struts.NetworkResponses;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Date: 8/14/15
 * Time: 2:58 PM
 *
 * @author brian
 */
public class ReportAjaxErrorAction extends NetworkAction {
    private String referenceId;
    private String error;
    private String stackTrace;
    private String debugHtml;

    @MethodDetails(requestType = HttpRequestType.AJAX, readOnly = ReadOnly.TRUE)
    @Override
    public String execute() throws Exception {
        if (!isEmpty(referenceId) && !isEmpty(stackTrace)) {
            UserSession userSession = UserSession.getUserSession();
            StatisticManager.recordAjaxError(null, userSession != null ? userSession.getUniqueVisitOid() : null, referenceId, error, stackTrace, debugHtml);
        }
        return NetworkResponses.emptyResponse();
    }

    @BypassHtmlDisable
    public void setReferenceId(String referenceId) {
        this.referenceId = referenceId;
    }

    @BypassHtmlDisable
    public void setError(String error) {
        this.error = error;
    }

    @BypassHtmlDisable
    public void setStackTrace(String stackTrace) {
        this.stackTrace = stackTrace;
    }

    @BypassHtmlDisable
    public void setDebugHtml(String debugHtml) {
        this.debugHtml = debugHtml;
    }
}
