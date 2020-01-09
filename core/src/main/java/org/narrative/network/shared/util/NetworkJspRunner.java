package org.narrative.network.shared.util;

import org.narrative.common.util.Task;
import org.narrative.common.web.jsp.InternalJSPRunner;
import org.narrative.network.core.system.NetworkRegistry;
import org.narrative.network.shared.context.NetworkContextHolder;
import org.narrative.network.shared.interceptors.ActionSetupInterceptor;
import org.narrative.network.shared.interceptors.NetworkContextInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Date: Jun 3, 2009
 * Time: 8:45:14 AM
 *
 * @author brian
 */
@Component
public class NetworkJspRunner {

    public static final String TASK_ATTRIBUTE = "task";

    private final InternalJSPRunner internalJSPRunner;

    @Autowired
    public NetworkJspRunner(InternalJSPRunner internalJSPRunner) {
        this.internalJSPRunner = internalJSPRunner;
    }

    public String runJsp(String jspFile, Task task, Map<String, Object> attributes) {
        // bl: put the extra attributes in first to ensure that none of the standard attributes can be overwritten.
        if (attributes == null) {
            attributes = newHashMap();
        }
        attributes.put(TASK_ATTRIBUTE, task);
        // bl: for interoperability with common tags between web requests and internal JSP requests,
        // add the task as a "contextHolder" attribute, too.  this way, common JSP tags can reference "contextHolder"
        // and give it a class name of AreaContextHolder or NetworkContextHolder and work for both web requests
        // and internal JSP requests for emails.
        attributes.put(ActionSetupInterceptor.REQUEST_ATTRIBUTE_CONTEXT_HOLDER, task);

        // jw: some places in the JSP reference the networkContext directly, so let's try and support that more naturally.
        if (!attributes.containsKey(NetworkContextInterceptor.REQUEST_ATTRIBUTE_NETWORK_CONTEXT) && task instanceof NetworkContextHolder) {
            NetworkContextHolder holder = (NetworkContextHolder) task;

            attributes.put(NetworkContextInterceptor.REQUEST_ATTRIBUTE_NETWORK_CONTEXT, holder.getNetworkContext());
        }

        attributes.put(NetworkRegistry.NETWORK_REGISTRY, NetworkRegistry.getInstance());
        return internalJSPRunner.runJsp(jspFile, attributes);
    }
}
