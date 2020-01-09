package org.narrative.network.shared.util;

import org.narrative.network.core.statistics.StatisticManager;
import org.apache.struts2.dispatcher.Dispatcher;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.Map;

/**
 * Need our own NetworkDispatcher so that we can hook into the error framework
 * for errors that happen during rendering of JSP.  This class enables us to detect these
 * errors and report them in the cluster admin errors pages.
 * <p>
 * Date: Oct 12, 2007
 * Time: 4:11:27 PM
 *
 * @author brian
 */
public class NetworkDispatcher extends Dispatcher {
    public NetworkDispatcher(ServletContext servletContext, Map<String, String> initParams) {
        super(servletContext, initParams);
    }

    @Override
    public void sendError(HttpServletRequest request, HttpServletResponse response, int code, Exception e) {
        StatisticManager.recordException(e, false, null);
        super.sendError(request, response, code, e);
    }
}
