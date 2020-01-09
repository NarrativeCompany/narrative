package org.narrative.network.shared.interceptors;

import org.narrative.common.util.IPStringUtil;
import org.narrative.common.util.UnexpectedError;
import org.narrative.common.util.processes.ActionProcess;
import org.narrative.common.util.processes.GenericProcess;
import org.narrative.common.util.processes.ProcessManager;
import org.narrative.common.util.processes.TraceProcessHistory;
import org.narrative.common.util.trace.TraceManager;
import org.narrative.network.shared.processes.ServletRequestProcess;
import org.narrative.network.shared.util.NetworkLogger;
import com.opensymphony.xwork2.ActionInvocation;
import org.apache.struts2.ServletActionContext;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by IntelliJ IDEA.
 * User: barry
 * Date: Dec 12, 2005
 * Time: 11:48:41 PM
 */
public class ActionProcessInterceptor extends NetworkStrutsInterceptorBase {

    private static final NetworkLogger logger = new NetworkLogger(ActionProcessInterceptor.class);

    public static final String PROCESS_OID_REQ_PARAM = "processOid";

    /**
     * Allows the Interceptor to do some processing on the request before and/or after the rest of the processing of the
     * request by the {@link com.opensymphony.xwork2.ActionInvocation} or to short-circuit the processing and just return a String return code.
     *
     * @return the return code, either returned from {@link com.opensymphony.xwork2.ActionInvocation#invoke()}, or from the interceptor itself.
     * @throws Exception any system-level error, as defined in {@link com.opensymphony.xwork2.Action#execute()}.
     */
    public String networkIntercept(ActionInvocation invocation) throws Exception {
        ActionProcess pi = new ActionProcess(invocation);
        HttpServletRequest req = ServletActionContext.getRequest();
        // bl: set the processOid here so that we can include it in the exception.jsp
        req.setAttribute(PROCESS_OID_REQ_PARAM, pi.getProcessOid());

        String oldName = Thread.currentThread().getName();
        String result = null;
        try {
            StringBuilder threadName = new StringBuilder();
            // bl: changing so that we always now include the original thread name as part of the name
            // so that we can associate the debug lines after changing the name
            threadName.append(oldName);
            threadName.append("-");
            threadName.append(pi.getProcessOid()).append("-");
            String actionName = invocation.getProxy().getActionName();
            if (IPStringUtil.isEmpty(actionName)) {
                actionName = IPStringUtil.getStringAfterLastIndexOf(invocation.getAction().getClass().getName(), ".");
            }
            threadName.append(actionName);
            threadName.append("!");
            threadName.append(invocation.getProxy().getMethod());
            Thread.currentThread().setName(threadName.toString());
            ProcessManager.getInstance().pushProcess(pi);

            GenericProcess rootProcess = pi.getRootProcess();
            if (!(rootProcess instanceof ServletRequestProcess)) {
                throw UnexpectedError.getRuntimeException("Found a root process for an action request that was not a ServletRequestProcess! should not be possible! class/" + rootProcess.getClass().getName());
            }
            //save any tracing info we might want
            if (TraceManager.isTracing()) {
                TraceProcessHistory item = (TraceProcessHistory) TraceManager.getRootTraceItem();
                item.setProcName(threadName.toString());
            }

            result = invocation.invoke();

            return result;
        } finally {
            try {
                StringBuilder logMessage = new StringBuilder();
                logMessage.append("\"").append(pi.isOutlier() ? "SLOW " : "").append("REQUEST_COMPLETED.  Duration: \"").append((long) pi.getTotalRunningTime()).append("ms");
                logMessage.append(" Result: ").append(result);
                logger.info(logMessage.toString());
                ProcessManager.getInstance().popProcess();
            } finally {
                // bl: make sure we ALWAYS set the thread name back when we're done
                Thread.currentThread().setName(oldName);
            }
        }
    }
}
