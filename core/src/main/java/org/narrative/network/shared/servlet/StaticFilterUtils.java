package org.narrative.network.shared.servlet;

import org.narrative.common.util.HtmlConstants;
import org.narrative.common.util.IPHTMLUtil;
import org.narrative.common.util.IPStringUtil;
import org.narrative.common.util.UnexpectedError;
import org.narrative.network.core.system.NetworkRegistry;
import org.narrative.network.shared.util.NetworkLogger;
import org.apache.catalina.connector.Request;
import org.apache.catalina.mapper.MappingData;

import javax.servlet.ServletRequest;
import javax.servlet.ServletRequestWrapper;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.lang.reflect.Field;
import java.util.regex.Pattern;

/**
 * Created by IntelliJ IDEA.
 * User: Paul
 * Date: Feb 17, 2006
 * Time: 9:43:37 AM
 */
public class StaticFilterUtils {
    private static final NetworkLogger logger = new NetworkLogger(StaticFilterUtils.class);

    // bl: all version strings should be of the form: /ver0.0.0-b123-abcdefg/
    // needed to tighten down this reg ex since previously we would detect strings like /vertical.gif anywhere
    // in the path and remove them, which breaks things.
    // note: can't test for this only at the start of strings (with ^) since JSP URLs use /jsp/ver0.0.0.0/xxx.jsp
    // and we want to strip out the /ver0.0.0-b123-abcdefg part while maintaining the /jsp part (since we strip from the
    // servlet path and not from path info).
    private static final Pattern VERSION_PATH_PATTERN = Pattern.compile("/ver(\\d+\\.){2}\\d+(-(LOCAL|((.+\\.)?SNAPSHOT)|RC|HOTFIX)-\\d+)?-\\p{Alnum}{7}/", Pattern.CASE_INSENSITIVE);

    public static boolean isValidVersionPath(String path) {
        return VERSION_PATH_PATTERN.matcher(path).matches();
    }

    public static void removeVersionPathFromPathInfo(HttpServletRequest request) {
        String pathInfo = request.getPathInfo();
        if (IPStringUtil.isEmpty(pathInfo)) {
            return;
        }
        String newPathInfo = VERSION_PATH_PATTERN.matcher(pathInfo).replaceAll("/");
        // nothing changed?  then just return
        if (pathInfo.equals(newPathInfo)) {
            return;
        }
        setPathInfo(request, newPathInfo);
    }

    public static void setPathInfo(HttpServletRequest request, String pathInfo) {
        request = getRequestFromRequest(request);
        Request oRequest = getCatalinaRequestFromRequestFacade(request);
        oRequest.setPathInfo(pathInfo);
    }

    private static HttpServletRequest getRequestFromRequest(HttpServletRequest request) {
        if (request instanceof GHttpServletRequest) {
            // get the inner RequestFacade object off of the GHttpServletRequest. that's what we set the PathInfo on.
            return ((GHttpServletRequest) request).getRequest();
        }
        return request;
    }

    public static void setServletPath(HttpServletRequest request, String servletPath) {
        request = getRequestFromRequest(request);
        Request oRequest = getCatalinaRequestFromRequestFacade(request);
        MappingData mappingData = oRequest.getMappingData();
        // bl: the wrapperPath is what holds the "servletPath" for the request
        mappingData.wrapperPath.setString(servletPath);
    }

    public static Request getCatalinaRequestFromRequestFacade(HttpServletRequest request) {
        try {
            //Handle wrapped servlet request when Spring Session is enabled
            ServletRequest dereferencedRequest = getUnwrappedRequest(request);

            Field requestField = dereferencedRequest.getClass().getDeclaredField("request");
            requestField.setAccessible(true);
            return (Request) requestField.get(dereferencedRequest);
        } catch (Throwable t) {
            throw UnexpectedError.getRuntimeException("Tomcat is currently the supported servlet container. Should be a org.apache.catalina.connector.RequestFacade with a request property that is of type org.apache.catalina.connector.Request. Static files won't work otherwise.", t, true);
        }
    }

    private static ServletRequest getUnwrappedRequest(ServletRequest request) {
        if(request instanceof ServletRequestWrapper) {
            return getUnwrappedRequest(((ServletRequestWrapper)request).getRequest());
        }
        if(request instanceof GHttpServletRequest) {
            return getUnwrappedRequest(((GHttpServletRequest)request).getRequest());
        }
        return request;
    }

    /**
     * This function checks the If-Modified-Since header against the servlet startup time, and either passes back
     * a 304 status, which is a signal to the client that the file hasn't changed, -or- it adds the Last-Modified
     * header to the response if we will be sending the file
     *
     * @param request
     * @param response
     * @return (true) if the file is cached on the client an no futher processing is required
     * (false) if the cleint needs a new copy of the file
     */
    public static boolean handleFileAgeCheck(HttpServletRequest request, HttpServletResponse response, final long lastModifiedTime) {
        //check to see if the file needs to be sent again
        boolean modified = request.getDateHeader(HtmlConstants.HEADER_IF_MODIFIED) < lastModifiedTime;

        if (logger.isDebugEnabled()) {
            logger.debug("Refresh file(" + modified + ")- header{" + request.getDateHeader(HtmlConstants.HEADER_IF_MODIFIED) + "} vs. startup{" + lastModifiedTime + "}");
        }

        // bl: if the version string was included in the path, then go ahead and set the Expires header for this
        // request to cache the file in the browser forever.
        // nb. do this even for 304 not modified requests.
        String pathInfo = request.getPathInfo();
        if (!IPStringUtil.isEmpty(pathInfo) && pathInfo.contains(NetworkRegistry.getInstance().getVersionStringForPath())) {
            IPHTMLUtil.setExpiresHeaderToOneYear(response);
        }

        if (!modified) {
            response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
            return true;
        } else {
            response.addDateHeader(HtmlConstants.HEADER_LAST_MODIFIED, lastModifiedTime);
            return false;
        }
    }

    public static String getStartupString() {
        // bl: have this in its own function since we want to delay getting the version until after the servlet
        // has initialized.
        return "NARRATIVE SERVLET STARTUP - " + getLogSuffix();
    }

    public static String getLogSuffix() {
        return NetworkRegistry.getInstance().getClusterId() + "-" + NetworkRegistry.getInstance().getServletName() + "-ver" + NetworkRegistry.getInstance().getVersion();
    }
}
