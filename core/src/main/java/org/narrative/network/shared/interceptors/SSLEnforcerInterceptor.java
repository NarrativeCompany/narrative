package org.narrative.network.shared.interceptors;

import org.narrative.common.util.UnexpectedError;
import org.narrative.common.web.RequestResponseHandler;
import org.narrative.common.web.struts.MethodPropertiesUtil;
import org.narrative.network.shared.struts.NetworkResponses;
import com.opensymphony.xwork2.ActionInvocation;

import static org.narrative.network.shared.util.NetworkCoreUtils.*;

/**
 * Date: Jan 8, 2010
 *
 * @author Barry
 */
public class SSLEnforcerInterceptor extends NetworkStrutsInterceptorBase {

    @Override
    protected String networkIntercept(ActionInvocation actionInvocation) throws Exception {
        RequestResponseHandler reqResp = networkContext().getReqResp();
        boolean isSslEnabledOnAuthRealm = networkContext().getAuthRealm().isSslEnabled();
        // bl: force SSL on areas that have SSL enabled and on requests that specifically require SSL only.
        boolean isSSLOnly = isSslEnabledOnAuthRealm || (MethodPropertiesUtil.isSSLOnlyRequest(actionInvocation));
        if (reqResp.isSecureRequest()) {
            // bl: if SSL is not enabled, then we need to redirect to non-SSL
            if (!isSSLOnly) {
                if (reqResp.isPost()) {
                    //There should never be an ssl post request to a non-ssl action, either programmer error or someone is hacking
                    throw UnexpectedError.getIgnorableRuntimeException("Tried posting via SSL when SSL not enabled");
                }
                // user tried accessing us on SSL domain, redirect
                return NetworkResponses.redirectResponse("http" + reqResp.getUrl().substring(reqResp.getScheme().length()), true);
            }
        } else {
            if (isSSLOnly) {
                if (reqResp.isPost()) {
                    //There should never be a non-ssl post request to an ssl action, either programmer error or someone is hacking
                    throw UnexpectedError.getIgnorableRuntimeException("Tried posting to SSL only request without SSL");
                } else {
                    // user tried accessing us on non-SSL domain, redirect
                    return NetworkResponses.redirectResponse("https" + reqResp.getUrl().substring(reqResp.getScheme().length()), true);
                }
            }
        }
        return actionInvocation.invoke();
    }
}