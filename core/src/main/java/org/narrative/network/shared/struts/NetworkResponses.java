package org.narrative.network.shared.struts;

import org.narrative.common.util.IPStringUtil;
import org.narrative.common.web.HttpServletRequestResponseHandler;
import org.narrative.common.web.RequestResponseHandler;
import org.narrative.common.web.struts.MethodPropertiesUtil;
import org.narrative.network.core.cluster.partition.PartitionGroup;
import org.narrative.network.shared.authentication.UserSession;
import org.narrative.network.shared.baseactions.NetworkAction;
import org.narrative.network.shared.services.HttpPostSessionObject;
import org.narrative.network.shared.services.NetworkServletRedirectResult;
import com.opensymphony.xwork2.ActionContext;
import org.apache.struts2.ServletActionContext;

import static org.narrative.common.util.CoreUtils.*;
import static org.narrative.network.shared.util.NetworkCoreUtils.*;

/**
 * Created by IntelliJ IDEA.
 * User: paul
 * Date: Dec 6, 2005
 * Time: 11:52:48 AM
 */
public class NetworkResponses {
    public static final String REDIRECT_ACTION_CONTEXT_PARAM = NetworkResponses.class.getName() + "-ActionContext-Redirect";
    public static final String LEGACY_AJAX_RELOAD_RESULT = "legacyAjaxReloadResponse";
    public static final String REDIRECT_RESPONSE = "redirect";
    private static final String DO_NOTHING_RESULT = "doNothingResult";
    private static final String LEGACY_AJAX_ERRORS_RESULT = "legacy_ajaxErrorsResult";
    private static final String LEGACY_AJAX_ERROR_MESSAGE_RESULT = "legacy_ajaxErrorMessageResult";
    private static final String EMPTY_RESPONSE = "empty";

    public static String legacyAjaxErrorsResult() {
        return LEGACY_AJAX_ERRORS_RESULT;
    }

    public static String legacyAjaxErrorMessageResult() {
        return LEGACY_AJAX_ERROR_MESSAGE_RESULT;
    }

    public static String getRedirectUrl(NetworkAction networkAction) {
        return NetworkServletRedirectResult.getRedirectUrl(networkAction, ServletActionContext.getRequest(), actionContext());
    }

    private static String ajaxReloadResponse() {
        return ajaxReloadResponse(true);
    }

    public static String ajaxReloadResponse(boolean storeRedirectUrlIfHttpPost) {
        NetworkAction action = getNetworkAction();
        RequestResponseHandler reqResp = networkContext().getReqResp();

        assert reqResp.isRequestedAsAjax() : "Should only request ajaxReloadResponse for requests made as AJAX!";

        // bl: use the same logic to determine the redirect url as we use in the NetworkServletRedirectResult.
        // this way, we can take advantage of things like the confirmation message parameters, etc.
        action.setRedirect(getRedirectUrl(action));

        assert !isEmpty(action.getRedirect()) : "Should always have a redirect when reaching this point in the code.";

        if (storeRedirectUrlIfHttpPost && UserSession.hasSession() && reqResp.isPost() && !PartitionGroup.getCurrentPartitionGroup().isReadOnly() && MethodPropertiesUtil.isPreventDoublePosting(action.getActionInvocation())) {
            // jw: persist the redirect value from this original request into any other duplicate posts
            final UserSession session = UserSession.getUserSession();
            PartitionGroup.addEndOfPartitionGroupRunnable(new Runnable() {
                public void run() {
                    // jw: if this was a post request lets get the users session and ensure that the redirect is recorded
                    //     in case we have any double submits.
                    if (session != null && reqResp.isPost()) {
                        synchronized (session) {
                            HttpPostSessionObject.HttpPostFormData formData = HttpPostSessionObject.getForUserSession(session).getFormData(reqResp.getParamValue(NetworkAction.FORM_TYPE_PARAM_NAME), reqResp, session, false);
                            if (formData != null) {
                                formData.setRedirect(action.getRedirect());
                            }
                        }
                    }
                }
            });
        }

        return LEGACY_AJAX_RELOAD_RESULT;
    }

    private static NetworkAction getNetworkAction() {
        return (NetworkAction) ActionContext.getContext().getActionInvocation().getAction();
    }

    public static String redirectResponse(String redirect) {
        return redirectResponse(redirect, false);
    }

    public static String redirectResponse(String redirect, boolean isPermanent) {
        // bl: if it was requested as AJAX, then just do an AJAX reload response.
        if (HttpServletRequestResponseHandler.isRequestedAsAjax(ServletActionContext.getRequest())) {
            // bl: set the redirect on the NetworkAction to make sure it has been setup properly!
            NetworkAction action = getNetworkAction();
            action.setRedirect(redirect);
            return ajaxReloadResponse();
        }
        setRedirectParamOnActionContext(redirect);
        return httpRedirectResponse(isPermanent);
    }

    public static String redirectResponse() {
        return redirectResponse(false);
    }

    public static String redirectResponse(boolean isPermanent) {
        // bl: if it was requested as AJAX, then just do an AJAX reload response.
        if (HttpServletRequestResponseHandler.isRequestedAsAjax(ServletActionContext.getRequest())) {
            return ajaxReloadResponse();
        }
        return httpRedirectResponse(isPermanent);
    }

    private static String httpRedirectResponse(boolean isPermanent) {
        if (isPermanent) {
            actionContext().put(NetworkServletRedirectResult.PERMANENT_REDIRECT_KEY, Boolean.TRUE);
        }
        return REDIRECT_RESPONSE;
    }

    public static void setRedirectParamOnActionContext(String redirect) {
        // bl: this will keep the redirect out of the url when it is empty
        if (!IPStringUtil.isEmpty(redirect)) {
            actionContext().put(REDIRECT_ACTION_CONTEXT_PARAM, redirect);
        }
    }

    public static String doNothingResponse() {
        return DO_NOTHING_RESULT;
    }

    public static String emptyResponse() {
        return EMPTY_RESPONSE;
    }

}
