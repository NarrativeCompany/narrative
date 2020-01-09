package org.narrative.network.shared.servlet;

import org.narrative.network.shared.util.NetworkDispatcher;
import org.apache.struts2.dispatcher.Dispatcher;
import org.apache.struts2.dispatcher.ng.HostConfig;
import org.apache.struts2.dispatcher.ng.InitOperations;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * This class exists so we can create a NetworkDispatcher when initializing the GStrutsPrepareAndExecuteFilter.
 * <p>
 * Date: 3/6/15
 * Time: 4:48 PM
 *
 * @author brian
 */
public class GInitOperations extends InitOperations {
    @Override
    public Dispatcher initDispatcher(HostConfig filterConfig) {
        Dispatcher dispatcher = createDispatcher(filterConfig);
        dispatcher.init();
        return dispatcher;
    }

    private Dispatcher createDispatcher(HostConfig filterConfig) {
        Map<String, String> params = new HashMap<String, String>();
        for (Iterator e = filterConfig.getInitParameterNames(); e.hasNext(); ) {
            String name = (String) e.next();
            String value = filterConfig.getInitParameter(name);
            params.put(name, value);
        }
        return new NetworkDispatcher(filterConfig.getServletContext(), params);
    }
}
