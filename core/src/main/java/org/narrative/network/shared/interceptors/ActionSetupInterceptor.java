package org.narrative.network.shared.interceptors;

import org.narrative.common.util.UnexpectedError;
import org.narrative.common.util.ValidationHandler;
import com.opensymphony.xwork2.ActionInvocation;
import org.apache.struts2.ServletActionContext;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by IntelliJ IDEA.
 * User: Paul
 * Date: Feb 17, 2006
 * Time: 10:22:21 AM
 */
public class ActionSetupInterceptor extends NetworkStrutsInterceptorBase {

    /**
     * bl: contextHolder is the common object we can use in common tags used by both web requests and internal JSP
     * tasks for emails.
     */
    public static final String REQUEST_ATTRIBUTE_CONTEXT_HOLDER = "contextHolder";

    public static final String REQUEST_ATTRIBUTE_ACTION = "action";
    /**
     * bl: sometimes in JSP, there are parameters named "action" (e.g. in form tags - gfs:form), in which case we can't refer
     * to the "action" attribute to access the NetworkAction since the "action" attribute has been overwritten.
     * to alleviate that problem, i'm adding a "networkAction" redundant request attribute to enable us to access
     * the network action in other places.
     * perhaps we should consider changing to always use networkAction instead of just plain "action"?
     */
    private static final String REQUEST_ATTRIBUTE_NETWORK_ACTION = "networkAction";

    protected String networkIntercept(ActionInvocation actionInvocation) throws Exception {

        HttpServletRequest req = ServletActionContext.getRequest();

        // bl: StrutsRequestWrapper allows searching the ognl value stack for JSP EL attributes
        // when the attribute isn't found by the "normal" JSP EL mechanism.  this seems like a bad
        // side-effect that we shouldn't rely on (and I don't think we ever use).  utilizing this would
        // allow us to have non-type/refactor safe JSP ELs, which we don't want.  see the implementation
        // directly in StrutsRequestWrapper for more information, but the idea is that putting this
        // special attribute in the ActionContext will completely prevent evaluation of JSP ELs
        // on the ognl value stack.
        actionInvocation.getInvocationContext().put("__requestWrapper.getAttribute", Boolean.TRUE);

        // bl: should never call this interceptor multiple times for the same request.  thus, we will no longer
        // be storing the original action and resetting the action on the request to the original when we are done.
        // this allows for us to set the action on the request once and it will remain on the request for the duration
        // of the request, which is necessary in order for JSP to have access to these request variables
        // in the NetworkServletDispatcherResult, which is used for all JSP responses now, including AJAX reload responses.
        if (req.getAttribute(REQUEST_ATTRIBUTE_ACTION) != null || req.getAttribute(REQUEST_ATTRIBUTE_NETWORK_ACTION) != null || req.getAttribute(REQUEST_ATTRIBUTE_CONTEXT_HOLDER) != null) {
            throw UnexpectedError.getRuntimeException("Found existing attributes on Request! Should not be possible, should it?!");
        }
        //set the action on the request so we can get at it in jsp
        ValidationHandler networkAction = getNetworkAction();
        req.setAttribute(REQUEST_ATTRIBUTE_ACTION, networkAction);
        req.setAttribute(REQUEST_ATTRIBUTE_NETWORK_ACTION, networkAction);
        req.setAttribute(REQUEST_ATTRIBUTE_CONTEXT_HOLDER, networkAction);

        //hand off to the next action
        return actionInvocation.invoke();
    }
}
