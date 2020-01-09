package org.narrative.network.shared.struts;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionProxy;
import com.opensymphony.xwork2.DefaultActionProxyFactory;

import java.util.Map;

/**
 * Date: 12/2/11
 * Time: 12:58 PM
 *
 * @author Jonmark Weber
 */
public class NetworkActionProxyFactory extends DefaultActionProxyFactory {
    @Override
    public ActionProxy createActionProxy(String namespace, String actionName, String methodName, Map<String, Object> extraContext, boolean executeResult, boolean cleanupContext) {
        // bl: start with the parameters currently on the ActionContext, which are set by NetworkActionMapper
        Map<String, Object> params = ActionContext.getContext().getParameters();
        // bl: if any params are set, then add them to the extraContext so that they will persist
        // through to the newly created ActionContext.
        if (params != null) {
            Map<String, Object> extraContextParams = (Map<String, Object>) extraContext.get(ActionContext.PARAMETERS);
            if (extraContextParams != null) {
                params.putAll(extraContextParams);
            }
            extraContext.put(ActionContext.PARAMETERS, params);
        }

        return super.createActionProxy(namespace, actionName, methodName, extraContext, executeResult, cleanupContext);
    }
}
