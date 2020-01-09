package org.narrative.common.util.processes;

import org.narrative.common.util.NarrativeLogger;
import org.narrative.common.util.IPHttpUtil;
import org.narrative.common.web.HttpServletRequestResponseHandler;
import org.narrative.common.web.RequestResponseHandler;

import javax.servlet.http.HttpServletRequest;

import java.util.Enumeration;
import java.util.Map;

/**
 * Date: 9/18/18
 * Time: 3:48 PM
 *
 * @author brian
 */
public abstract class RequestProcessBase extends GenericProcess {
    public RequestProcessBase(String name, Thread thread) {
        super(name, thread);
    }

    public abstract NarrativeLogger getLogger();
    public abstract HttpServletRequest getHttpServletRequest();
    public abstract Map getParameters();

    public String getInfo() {
        // can't use ServletActionContext since this method won't necessarily be called from within the
        // same thread.
        //ServletActionContext.getRequest()
        HttpServletRequest request = getHttpServletRequest();
        StringBuilder info = new StringBuilder();
        info.append("Parameters: [").append(RequestResponseHandler.getParametersString(getParameters())).append("]\n");
        info.append("Requestor IP: ").append(HttpServletRequestResponseHandler.getRemoteHostIpFromRequest(request, false)).append("\n");
        // bl: changing to output all headers when debug is enabled on the ActionProcess log.
        if (getLogger().isDebugEnabled()) {
            Enumeration headerNames = request.getHeaderNames();
            while (headerNames.hasMoreElements()) {
                String headerName = (String) headerNames.nextElement();
                info.append(headerName).append(": ").append(request.getHeader(headerName)).append("\n");
            }
        } else {
            info.append("Browser Type: ").append(IPHttpUtil.getUserAgent(request)).append("\n");
            info.append("Referer: ").append(IPHttpUtil.getReferrerHeader(request)).append("\n");
        }
        return info.toString();
    }

    public RequestProcessHistory getHistory() {
        return new RequestProcessHistory(this);
    }
}
