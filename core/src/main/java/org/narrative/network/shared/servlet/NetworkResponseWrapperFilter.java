package org.narrative.network.shared.servlet;

import org.narrative.common.util.IPUtil;
import org.narrative.common.util.UnexpectedError;
import org.narrative.common.web.HttpServletRequestResponseHandler;
import org.narrative.network.core.statistics.StatisticManager;
import org.narrative.network.core.system.NetworkRegistry;
import org.narrative.network.shared.util.NetworkLogger;
import org.springframework.stereotype.Component;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Date: Nov 19, 2009
 * Time: 3:05:18 PM
 *
 * @author brian
 */
@Component
public class NetworkResponseWrapperFilter implements Filter {
    private static final NetworkLogger logger = new NetworkLogger(NetworkResponseWrapperFilter.class);

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        logger.info("NetworkResponseWrapperFilter.init");
    }

    @Override
    public void destroy() {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        //if not http then just delegate
        if ((!(request instanceof HttpServletRequest) || !(response instanceof HttpServletResponse))) {
            chain.doFilter(request, response);
            return;
        }

        // bl: if the app hasn't fully initialized yet, then wait until it is to avoid random errors at startup.
        // typically these are monitoring requests (e.g. /actuator/info). the heartbeat server will not have been
        // started yet, so our nginx ingress controller shouldn't be sending any "real" user requests through yet
        while(!NetworkRegistry.getInstance().isInitDone()) {
            IPUtil.uninterruptedSleep(1000);
        }

        GHttpServletResponse gResponse = new GHttpServletResponse((HttpServletResponse) response);
        GHttpServletRequest gRequest = new GHttpServletRequest((HttpServletRequest) request, gResponse);

        gResponse.addHeader("X-Backend", NetworkRegistry.getInstance().getServletName());
        gResponse.addHeader("X-Narrative-Version", NetworkRegistry.getInstance().getVersion());

        try {
            chain.doFilter(gRequest, gResponse);
        } catch (Throwable t) {
            StatisticManager.recordException(t, true /* filter exception */, new HttpServletRequestResponseHandler(gRequest, gResponse, false));
            throw UnexpectedError.getRuntimeException("Failed executing request.  Exception bubbled up to NetworkResponseWrapperFilter.", t, true);
        }
    }
}
