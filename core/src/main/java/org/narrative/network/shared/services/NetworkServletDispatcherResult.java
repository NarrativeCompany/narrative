package org.narrative.network.shared.services;

import org.narrative.common.util.IPHttpUtil;
import org.narrative.common.util.processes.TraceProcessHistory;
import org.narrative.common.util.trace.TraceItem;
import org.narrative.common.util.trace.TraceManager;
import org.narrative.common.web.RequestResponseHandler;
import org.narrative.common.web.struts.MethodPropertiesUtil;
import com.opensymphony.xwork2.ActionInvocation;
import org.apache.struts2.dispatcher.ServletDispatcherResult;

import static org.narrative.network.shared.util.NetworkCoreUtils.*;

/**
 * Created by IntelliJ IDEA.
 * User: Paul
 * Date: Nov 1, 2006
 * Time: 10:46:27 PM
 */
public class NetworkServletDispatcherResult extends ServletDispatcherResult {

    private static final String OUTPUT_PARAM = "output";
    private static final String JSON_OUTPUT_PARAM_VALUE = "json";
    private static final String STOCK_OUTPUT_PARAM_VALUE = "stock";

    public void doExecute(String result, ActionInvocation actionInvocation) throws Exception {
        setNoCacheHeadersIfNecessary(actionInvocation);

        TraceItem ti = null;
        if (TraceManager.isTracing()) {
            ti = TraceManager.startTrace(new TraceProcessHistory("jsp"));
        }

        try {
            super.doExecute(result, actionInvocation);
        } finally {
            if (ti != null) {
                TraceManager.endTrace(ti);
            }
        }
    }

    public static void setNoCacheHeadersIfNecessary(ActionInvocation actionInvocation) {
        setNoCacheHeadersIfNecessary(networkContext().getReqResp(), actionInvocation);
    }

    public static void setNoCacheHeadersIfNecessary(RequestResponseHandler responseHandler, ActionInvocation actionInvocation) {
        // bl: for ajax GET requests, we want to make sure that browsers do not cache the response.  IE seems
        // to be the worst at this.  thus, set the no cache header for these.
        // todo: do we want to bypass these headers for RSS requests?
        if (IPHttpUtil.isMethodGet(responseHandler.getMethod())) {
            responseHandler.setNoCacheHeaders();
        }
    }
}
