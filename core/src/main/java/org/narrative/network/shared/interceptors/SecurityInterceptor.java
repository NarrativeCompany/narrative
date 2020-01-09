package org.narrative.network.shared.interceptors;

import org.narrative.network.shared.baseactions.NetworkAction;
import com.opensymphony.xwork2.ActionInvocation;

/**
 * Date: Jun 12, 2006
 * Time: 9:31:00 AM
 *
 * @author Brian
 */
public class SecurityInterceptor extends NetworkStrutsInterceptorBase {

    private static final String HAS_RUN_SECURITY_INTERCEPTOR = SecurityInterceptor.class.getName() + "-HasRunSecurityInterceptor";

    protected String networkIntercept(ActionInvocation actionInvocation) throws Exception {
        // bl: track the fact that we have run the SecurityInterceptor for this ActionInvocation
        actionInvocation.getInvocationContext().put(HAS_RUN_SECURITY_INTERCEPTOR, Boolean.TRUE);
        return networkSecurityIntercept(getNetworkAction(), actionInvocation, false);
    }

    static String networkSecurityIntercept(NetworkAction networkAction, ActionInvocation actionInvocation, boolean isAfterParams) throws Exception {
        // bl: we only want to do the security check here if we've already run the SecurityInterceptor (which means
        // that security checks are otherwise in the stack). if we haven't, then we don't want to do the security checks at all.
        // need this in order to avoid issues with getting PrimaryRole via NetworkCommonPostParametersInterceptor for network-noauth requests.
        {
            Boolean hasRunSecurityInterceptor = (Boolean) actionInvocation.getInvocationContext().get(HAS_RUN_SECURITY_INTERCEPTOR);
            if (hasRunSecurityInterceptor == null || !hasRunSecurityInterceptor) {
                return actionInvocation.invoke();
            }
        }
        // note: no special handling for master AccessViolations.  just let the normal error handler take care of it.
        if (isAfterParams) {
            networkAction.checkRightAfterParams();
        } else {
            networkAction.checkRight();
        }

        return actionInvocation.invoke();
    }
}
