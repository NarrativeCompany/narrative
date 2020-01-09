package org.narrative.network.shared.interceptors;

import org.narrative.network.shared.context.AreaContext;
import org.narrative.network.shared.context.AreaContextAware;
import org.narrative.network.shared.context.NetworkContext;
import org.narrative.network.shared.context.NetworkContextAware;
import com.opensymphony.xwork2.ActionInvocation;
import org.apache.struts2.ServletActionContext;

import javax.servlet.http.HttpServletRequest;

import static org.narrative.network.shared.util.NetworkCoreUtils.*;

/**
 * Created by IntelliJ IDEA.
 * User: paul
 * Date: Dec 9, 2005
 * Time: 9:44:28 AM
 */
public class NetworkContextInterceptor extends NetworkStrutsInterceptorBase {

    public static final String REQUEST_ATTRIBUTE_NETWORK_CONTEXT = "networkContext";

    public String networkIntercept(ActionInvocation actionInvocation) throws Exception {
        //set it on any actions that care about it
        Object action = actionInvocation.getAction();

        NetworkContext networkContext = networkContext();

        if (action instanceof AreaContextAware && networkContext instanceof AreaContext) {
            ((AreaContextAware) action).setAreaContext((AreaContext) networkContext);
        }

        assert action instanceof NetworkContextAware : "NetworkAction is our base action and must implement NetworkContextAware.";
        HttpServletRequest req = ServletActionContext.getRequest();
        ((NetworkContextAware) action).setNetworkContext(networkContext);
        Object oldNetworkContext = req.getAttribute(REQUEST_ATTRIBUTE_NETWORK_CONTEXT);
        try {
            req.setAttribute(REQUEST_ATTRIBUTE_NETWORK_CONTEXT, networkContext);
            return actionInvocation.invoke();
        } finally {
            req.setAttribute(REQUEST_ATTRIBUTE_NETWORK_CONTEXT, oldNetworkContext);
        }
    }
}
