package org.narrative.network.shared.interceptors;

import org.narrative.common.util.UnexpectedError;
import org.narrative.common.util.ValidationHandler;
import org.narrative.network.shared.baseactions.NetworkAction;
import org.narrative.network.shared.context.NetworkContextInternal;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.Interceptor;

/**
 * This base class should be used for all network interceptors.  This class implements
 * both Struts's Interceptor interface as well as our own NetworkInterceptor interface
 * that is used for "internal" jobs such as scheduled tasks.
 * Date: Dec 21, 2005
 * Time: 8:57:10 AM
 *
 * @author Brian
 */
public abstract class NetworkStrutsInterceptorBase implements Interceptor {
    public void destroy() {}

    public void init() {}

    protected final ThreadLocal<NetworkContextInternal> networkContext = new ThreadLocal<NetworkContextInternal>();
    private final ThreadLocal<NetworkAction> networkAction = new ThreadLocal<NetworkAction>();

    public final String intercept(ActionInvocation actionInvocation) throws Exception {
        assert actionInvocation.getAction() instanceof ValidationHandler : "Action must descend network action";
        NetworkAction networkAction = (NetworkAction) actionInvocation.getAction();
        NetworkContextInternal old = networkContext.get();
        networkContext.set((NetworkContextInternal) networkAction.getNetworkContext());
        NetworkAction oldAction = this.networkAction.get();
        this.networkAction.set(networkAction);
        try {
            return networkIntercept(actionInvocation);
        } finally {
            networkContext.set(old);
            this.networkAction.set(oldAction);
        }
    }

    protected abstract String networkIntercept(ActionInvocation actionInvocation) throws Exception;

    protected NetworkContextInternal getNetworkContext() {
        NetworkContextInternal ret = networkContext.get();
        if (ret == null) {
            throw UnexpectedError.getRuntimeException("Can't get NetworkContext from NetworkStrutsInterceptorBase until after the NetworkContextInterceptor has been run!");
        }
        return networkContext.get();
    }

    protected NetworkAction getNetworkAction() {
        return networkAction.get();
    }

}
