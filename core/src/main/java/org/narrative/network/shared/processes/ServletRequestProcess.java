package org.narrative.network.shared.processes;

import org.narrative.common.util.processes.GenericProcess;
import org.narrative.network.shared.servlet.GHttpServletResponse;

import javax.servlet.http.HttpServletRequest;

/**
 * Date: Nov 20, 2009
 * Time: 7:55:24 AM
 *
 * @author brian
 */
public class ServletRequestProcess extends GenericProcess {
    private final HttpServletRequest request;
    private final GHttpServletResponse response;

    public ServletRequestProcess(String name, HttpServletRequest request, GHttpServletResponse response) {
        super(name);
        this.request = request;
        this.response = response;
    }

    public HttpServletRequest getRequest() {
        return request;
    }

    public GHttpServletResponse getResponse() {
        return response;
    }

    @Override
    public String getAreaName() {
        return "{servletRequest}";
    }

    @Override
    public String getOwner() {
        return "{unknown}";
    }

}
