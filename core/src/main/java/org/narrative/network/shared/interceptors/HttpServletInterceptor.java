package org.narrative.network.shared.interceptors;

import org.narrative.common.web.RequestResponseHandler;
import org.narrative.network.shared.baseactions.RequestResponseAware;
import org.narrative.network.shared.context.NetworkContextInternal;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.PreResultListener;

/**
 * Created by IntelliJ IDEA.
 * User: paul
 * Date: Dec 2, 2005
 * Time: 10:26:50 AM
 */
public class HttpServletInterceptor extends NetworkStrutsInterceptorBase {

    private static class HttpCookiePreResultListener implements PreResultListener {

        private final RequestResponseHandler reqResp;

        public HttpCookiePreResultListener(RequestResponseHandler reqResp) {
            this.reqResp = reqResp;
        }

        public void beforeResult(ActionInvocation invocation, String resultCode) {
            // send the pending cookies
            // todo: what if there is an error/exception during action processing?
            // do we want to not send pending cookies in that case?  or do we just assume
            // that if a pending cookie was set, it was intended to be set and thus it will
            // be sent in the response, regardless of whether or not the code that follows
            // results in an error or exception.
            // todo: also, what about doing different things based on the result code?
            // LF: if the request is secure set cookies as secure
            reqResp.sendPendingCookies(reqResp.isSecureRequest());
        }
    }

    public String networkIntercept(ActionInvocation actionInvocation) throws Exception {

        NetworkContextInternal networkContextInternal = getNetworkContext();
        RequestResponseHandler reqResp = networkContextInternal.getReqResp();

        // make sure we only do the initialization and setting on the NetworkContext once per request.
        // likewise for adding the pre-result listener.
        assert reqResp != null : "Found a null reqResp object in the interceptor stack.  The value should always have been set in the NetworkFilter! Must be a coding bug somewhere (perhaps a ThreadLocal not being unset somewhere?).";

        // add a PreResultListener to make sure the pending cookies from the RequestResponseHandler
        // are properly sent in the response.
        actionInvocation.addPreResultListener(new HttpCookiePreResultListener(reqResp));

        Object action = actionInvocation.getAction();

        //make sure the action needs the info
        if (action instanceof RequestResponseAware) {
            // set the RequestResponseHandler on the action always.
            ((RequestResponseAware) action).setRequestResponse(reqResp);
        }
        return actionInvocation.invoke();
    }
}
