package org.narrative.common.util.processes;

import org.narrative.common.util.NarrativeLogger;
import org.narrative.network.shared.util.NetworkLogger;

import javax.servlet.http.HttpServletRequest;

import java.util.Map;

/**
 * Date: 9/18/18
 * Time: 3:48 PM
 *
 * @author brian
 */
public class SpringProcess extends RequestProcessBase {
    private static final NetworkLogger logger = new NetworkLogger(SpringProcess.class);

    private final HttpServletRequest request;

    public SpringProcess(String name, HttpServletRequest request) {
        super(name, null);
        this.request = request;
    }

    @Override
    public NarrativeLogger getLogger() {
        return logger;
    }

    @Override
    public HttpServletRequest getHttpServletRequest() {
        return request;
    }

    @Override
    public Map getParameters() {
        return request.getParameterMap();
    }

}
