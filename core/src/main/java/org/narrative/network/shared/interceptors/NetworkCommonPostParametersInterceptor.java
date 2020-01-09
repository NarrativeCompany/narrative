package org.narrative.network.shared.interceptors;

import org.narrative.common.util.IPStringUtil;
import org.narrative.common.util.PageNotFoundError;
import org.narrative.network.core.system.NetworkRegistry;
import org.narrative.network.shared.struts.NetworkActionMapper;
import com.opensymphony.xwork2.ActionInvocation;

import static org.narrative.network.shared.util.NetworkCoreUtils.*;

/**
 * Created by IntelliJ IDEA.
 * User: paul
 * Date: Jun 26, 2006
 * Time: 3:11:00 PM
 * This is an interceptor for any actions which need to occur after the prepare and all the parameters have been set.
 */
public class NetworkCommonPostParametersInterceptor extends NetworkStrutsInterceptorBase {
    protected String networkIntercept(ActionInvocation actionInvocation) throws Exception {
        String error = networkContext().getContextData(NetworkActionMapper.INVALID_ACTION_CONTEXT_PARAM);
        if (!IPStringUtil.isEmpty(error)) {
            // bl: on dev & QA servers, use the message we generated. on all other environments, use a generic message.
            throw new PageNotFoundError(NetworkRegistry.getInstance().isLocalOrDevServer() ? error : wordlet("error.pageNotFound"));
        }

        actionInvocation.getInvocationContext().put(NetworkExceptionInterceptor.HAVE_PARAMETERS_BEEN_SET_FOR_REQUEST_CONTEXT_ATTRIBUTE, Boolean.TRUE);

        // bl: also need to do the 2nd checkRight once all the parameters have been set.
        return SecurityInterceptor.networkSecurityIntercept(getNetworkAction(), actionInvocation, true);
    }
}
