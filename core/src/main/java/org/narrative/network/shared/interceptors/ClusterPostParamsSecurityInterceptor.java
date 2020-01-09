package org.narrative.network.shared.interceptors;

import com.opensymphony.xwork2.ActionInvocation;

/**
 * Date: Feb 23, 2015
 * Time: 3:07:00 PM
 *
 * @author Brian
 */
public class ClusterPostParamsSecurityInterceptor extends NetworkStrutsInterceptorBase {

    protected String networkIntercept(ActionInvocation actionInvocation) throws Exception {
        // bl: do the post param security check!
        getNetworkAction().checkRightAfterParams();
        return actionInvocation.invoke();
    }
}
