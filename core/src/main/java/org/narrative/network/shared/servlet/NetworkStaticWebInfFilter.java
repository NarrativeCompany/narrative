package org.narrative.network.shared.servlet;

import org.narrative.common.util.IPHTMLUtil;
import org.narrative.common.util.IPStringUtil;
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
 * This filter's sole purpose is to prevent access to the WEB-INF and META-INF
 * directories.  This filter is necessary since Tomcat's DefaultServlet implementation
 * doesn't protect against those directories being accessed relative to a servlet-path.
 * For example, if you have the path /static-legacy/* mapped to the DefaultServlet ("default"),
 * a request to /static-legacy/WEB-INF/web.xml would successfully return the web.xml.
 * With this filter in place, the resources in WEB-INF and META-INF will not be accessible
 * for these types of mappings to Tomcat's DefaultServlet.
 * <p>
 * Date: Dec 29, 2005
 * Time: 2:19:55 PM
 *
 * @author Brian
 */

@Component
public class NetworkStaticWebInfFilter implements Filter {
    private static final NetworkLogger logger = new NetworkLogger(NetworkStaticWebInfFilter.class);

    public void init(FilterConfig filterConfig) throws ServletException {
        logger.info("NetworkStaticWebInfFilter.init");
    }

    public void destroy() {}

    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        NetworkFilter.setStandardHttpHeadersForAllResponses(response);

        // if using a not-modified 304 response, then we're done here.
        if (StaticFilterUtils.handleFileAgeCheck(request, response, NetworkRegistry.getInstance().getGlobalLastModifiedTime())) {
            return;
        }

        StaticFilterUtils.removeVersionPathFromPathInfo(request);
        // bl: in newer versions of Tomcat (e.g. 6.0.32, but not 6.0.29 or 6.0.30), the DefaultServlet.getRelativePath(HttpServletRequest request)
        // method changed in that it will now pre-pend the servletPath from the request to the path info in order to
        // look up the file.  since the servlet path for these requests is /static-legacy/, we do *not* want it to be included
        // when looking up the file.  thus, simply set the servletPath to the empty string, which solves the issue.
        // note that this should be a non-issue for previous versions of tomcat where the servlet path was being ignored anyway.
        StaticFilterUtils.setServletPath(request, "");

        String pathInfo = request.getPathInfo();
        // do we need to test the path info?
        if (!IPStringUtil.isEmpty(pathInfo)) {
            // check if the path info starts with WEB-INF or META-INF, and if it does, return an error.
            // nb. also don't want to serve the files from the jsp folder.
            if (pathInfo.toUpperCase().startsWith("/WEB-INF") || pathInfo.toUpperCase().startsWith("/META-INF") || pathInfo.toLowerCase().startsWith("/jsp")) {
                String requestURI = IPHTMLUtil.getURLDecodedString(request.getRequestURI());
                response.sendError(HttpServletResponse.SC_NOT_FOUND, requestURI);
                return;
            }
        }

        // valid URL, so continue down the filter chain, if the file doesn't need caching
        try {
            filterChain.doFilter(servletRequest, servletResponse);
        } catch (Throwable t) {
            GHttpServletResponse gHttpServletResponse = new GHttpServletResponse(response);
            StatisticManager.recordException(t, true /* filter exception */, new HttpServletRequestResponseHandler(new GHttpServletRequest(request, gHttpServletResponse), gHttpServletResponse, false));
            throw UnexpectedError.getRuntimeException("Failed executing request.  Exception bubbled up to NetworkStaticWebInfFilter.", t, true);
        }
    }
}
