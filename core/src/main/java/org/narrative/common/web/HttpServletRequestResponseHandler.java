package org.narrative.common.web;

import org.narrative.common.persistence.ObjectPair;
import org.narrative.common.util.ClientAgentInformation;
import org.narrative.common.util.NarrativeConstants;
import org.narrative.common.util.NarrativeLogger;
import org.narrative.common.util.IPHTMLUtil;
import org.narrative.common.util.IPHttpUtil;
import org.narrative.common.util.IPStringUtil;
import org.narrative.common.util.IPUtil;
import org.narrative.common.util.UnexpectedError;
import org.narrative.network.shared.servlet.GHttpServletRequest;
import com.opensymphony.xwork2.ActionContext;
import org.apache.struts2.dispatcher.SessionMap;
import org.jetbrains.annotations.Nullable;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Created by IntelliJ IDEA.
 * User: paul
 * Date: Dec 2, 2005
 * Time: 10:55:04 AM
 * This class encapuslates the application from having to know about http requests and responses.
 */
public class HttpServletRequestResponseHandler extends RequestResponseHandler {

    private static final NarrativeLogger logger = new NarrativeLogger(HttpServletRequestResponseHandler.class);

    private final HttpServletRequest request;
    private final HttpServletResponse response;
    private final boolean dontAllowParamFetching;
    private ClientAgentInformation clientAgentInformation;
    private final Map<ObjectPair<String, String>, Cookie> cookieNameAndPathToPendingCookie = new HashMap<ObjectPair<String, String>, Cookie>();
    private String keyForClientRequest;
    private String md5KeyForClientRequest;
    private Object requestBodyObject;
    private MultipartHttpServletRequest multipartRequest;

    public HttpServletRequestResponseHandler(HttpServletRequest request, HttpServletResponse response, boolean dontAllowParamFetching) {
        this.request = request;
        this.response = response;
        this.dontAllowParamFetching = dontAllowParamFetching;
    }

    @Override
    public boolean isRawRequestBody() {
        return ((GHttpServletRequest) request).isRawRequestBody();
    }

    public String getRequestBody() {
        return ((GHttpServletRequest) request).getRequestBody();
    }

    public Cookie[] getCookies() {
        return request.getCookies();
    }

    public void addPendingCookie(Cookie cookie) {
        if (IPStringUtil.isEmpty(cookie.getPath())) {
            cookie.setPath("/");
        }
        cookieNameAndPathToPendingCookie.put(new ObjectPair<String, String>(cookie.getName(), cookie.getPath()), cookie);
    }

//    public UserSession getUserSession() {
//        return (UserSession) session.getAttribute(RequestResponseHandler.SESSION_NAME);
//    }
//
//    public UserSession createUserSession(Role role) {
//        UserSession userSession =  new UserSession(role, actionContext.getProtocolHandler());
//        session.setAttribute(RequestResponseHandler.SESSION_NAME, userSession);
//        return userSession;
//    }

    @Override
    public String getContentType() {
        return request.getContentType();
    }

    public Enumeration getHeaderNames() {
        return request.getHeaderNames();
    }

    public String getHeader(String name) {
        return request.getHeader(name);
    }

    public long getDateHeader(String name) {
        return request.getDateHeader(name);
    }

    @Nullable
    public Cookie getCookieFromRequest(String name) {
        if (request.getCookies() == null) {
            return null;
        }
        for (Cookie cookie : request.getCookies()) {
            if (name.equals(cookie.getName())) {
                return cookie;
            }
        }
        return null;

    }

    @Override
    public String getCookieValueFromRequest(String name) {
        Cookie cookie = getCookieFromRequest(name);
        return cookie == null ? null : cookie.getValue();
    }

    @Nullable
    public Cookie getPendingCookie(String name, String path) {
        if (IPStringUtil.isEmpty(path)) {
            path = "/";
        }
        return cookieNameAndPathToPendingCookie.get(new ObjectPair<String, String>(name, path));
    }

    public boolean isSecureRequest() {
        return isSecureRequest(request);
    }

    public static boolean isSecureRequest(HttpServletRequest request) {
        if ("https".equalsIgnoreCase(request.getHeader(FORWARDED_PROTOCOL_HEADER))) {
            return true;
        }
        if (!isEmpty(request.getHeader(IS_SECURE_HEADER))) {
            return true;
        }
        // jw: HA proxy will flag the request as being secure via the header above. For some clusters we will be proxying
        //     all requests (http included) from haproxy to the https connector.
        //return IPStringUtil.isStringEqualIgnoreCase(request.getScheme(), "https");
        return false;
    }

    @Override
    public boolean isRequestedAsAjax() {
        return isRequestedAsAjax(request);
    }

    public static boolean isRequestedAsAjax(HttpServletRequest request) {
        String xRequestedWithHeader = request.getHeader("X-Requested-With");
        return "XMLHttpRequest".equals(xRequestedWithHeader);
    }

    public String getMethod() {
        return request.getMethod();
    }

    public String getPath() {
        // bl: getPathInfo doesn't properly return the path if the path contains URL-encoded URLs.
        // instead, we should use getRequestURI, which will include the servlet path, but since we
        // always use a root context, we're ok to use it.  just like in NetworkActionMapper,
        // this will have to change if we ever want to run the servlet outside of the root context.
        //return request.getPathInfo();
        return request.getRequestURI();
    }

    @Override
    public String getPathAndQuery() {
        StringBuilder ret = new StringBuilder();
        ret.append(getPath());
        String queryString = request.getQueryString();
        if (!IPStringUtil.isEmpty(queryString)) {
            ret.append("?");
            ret.append(queryString);
        }
        return ret.toString();
    }

    public String getURI() {
        return request.getRequestURI();
    }

    public String getHost() {
        return request.getServerName();
    }

    public String getQueryString() {
        return request.getQueryString();
    }

    public String getUrl() {
        return getUrlFromRequest(request);
    }

    public String getBaseUrl() {
        return getBaseUrlFromRequest(request);
    }

    public static String getUrlFromRequest(HttpServletRequest request) {
        StringBuilder ret = new StringBuilder();
        ret.append(getBaseUrlFromRequest(request));
        String queryString = request.getQueryString();
        if (!IPStringUtil.isEmpty(queryString)) {
            ret.append("?");
            ret.append(queryString);
        }
        return ret.toString();
    }

    public static String getBaseUrlFromRequest(HttpServletRequest request) {
        String baseUrl = request.getRequestURL().toString();
        // bl: since pound proxies the request from SSL to non-SSL, we need to detect if the request is SSL
        // based on the Is-Secure header. if it is supposed to be SSL, then replace http:// with https://
        // this was causing infinite redirects on SSL sites when we were detecting if pretty URLs were working
        // properly or not.
        if (isSecureRequest(request) && baseUrl.startsWith("http://")) {
            return baseUrl.replaceFirst(Pattern.quote("http://"), Matcher.quoteReplacement("https://"));
        }
        // jw: we need the correlary
        if (!isSecureRequest(request) && baseUrl.startsWith("https://")) {
            return baseUrl.replaceFirst(Pattern.quote("https://"), Matcher.quoteReplacement("http://"));
        }
        return baseUrl;
    }

    public Locale getLocale() {
        return request.getLocale();
    }

    public String getReferrer() {
        return IPHttpUtil.getReferrerHeader(request);
    }

    public String getScheme() {
        // bl: if it's a secure request, we should return https. now that we may be using SSL between HAProxy and Tomcat,
        // let's just return http vs. https depending on whether we identify this as a secure request or not.
        return isSecureRequest() ? "https" : "http";
        // bl: can no longer trust the scheme off of the HttpServletRequest directly.
        //return request.getScheme();
    }

    public String getRemoteHostIp() {
        return getRemoteHostIpFromRequest(request, true);
    }

    public String getUnmassagedRemoteHostIp() {
        return getRemoteHostIpFromRequest(request, false);
    }

    public static String getRemoteHostIpFromRequest(HttpServletRequest request, boolean massage) {
        // bl: if using the load balancer, the remote host IP won't be accurate.
        // thus, in that case, use the x-forwarded-for header, which should contain the real remote host ip.
        // just check if the header exists, and if it does, use it.  if not, use the regular remote host addr.
        String ip;
        String xForwardedFor = request.getHeader("x-forwarded-for");
        if (!IPStringUtil.isEmpty(xForwardedFor)) {
            ip = xForwardedFor;
        } else {
            // no x-forwarded-for header?  then just use the remote addr.
            ip = request.getRemoteAddr();
        }
        if (!massage) {
            return ip;
        }
        return getMassagedIp(ip);
    }

    private static String getMassagedIp(String ip) {
        if (ip == null) {
            return null;
        }
        // always trim spaces from the IP
        ip = ip.trim();
        String[] splitVals = ip.split(",");
        // bl: just use the first IP given
        if (splitVals.length > 1) {
            for (String splitVal : splitVals) {
                String newIp = getValidIpAddress(splitVal);
                if (newIp != null) {
                    if (logger.isInfoEnabled()) {
                        logger.info("IP split by comma; using " + newIp + " instead of " + ip);
                    }
                    return newIp;
                }
            }
            // if there was a comma in the value, but we couldn't extract a valid IP address, then we're done.
            return null;
        }

        return getValidIpAddress(ip);
    }

    private static String getValidIpAddress(String ip) {
        if (ip == null) {
            return null;
        }
        ip = ip.trim();
        // bl: sometimes we'll get an x-forwarded-for equal to the string "unknown".  in those cases, just use null.
        // exclude empty IPs and IPs that are equal to "unknown"
        if (ip.isEmpty() || IPUtil.isEqual("unknown", ip)) {
            return null;
        }
        // make sure the IP is still a valid IP
        if (ip.length() > NarrativeConstants.MAX_IP_ADDRESS_LENGTH) {
            if (logger.isWarnEnabled()) {
                logger.warn("Truncating user IP from " + ip.length() + " to " + NarrativeConstants.MAX_IP_ADDRESS_LENGTH + " ip/" + ip);
            }
            return ip.substring(0, NarrativeConstants.MAX_IP_ADDRESS_LENGTH);
        }
        // bl: if it doesn't at least meet our minimum IP address length requirement, then skip it.
        if (ip.length() < NarrativeConstants.MIN_IP_ADDRESS_LENGTH) {
            return null;
        }
        return ip;
    }

    public static void main(String[] args) {
        List<ObjectPair<String, String>> pairs = new LinkedList<ObjectPair<String, String>>();
        pairs.add(new ObjectPair<String, String>("127.0.0.1, 127.0.0.1", "127.0.0.1"));
        pairs.add(new ObjectPair<String, String>("127.0.0.1", "127.0.0.1"));
        pairs.add(new ObjectPair<String, String>("unknown, 127.0.0.1", "127.0.0.1"));
        pairs.add(new ObjectPair<String, String>("unknown,127.0.0.1", "127.0.0.1"));
        pairs.add(new ObjectPair<String, String>("127.0.0.111   ,127.0.0.1", "127.0.0.111"));
        for (ObjectPair<String, String> pair : pairs) {
            String testIp = pair.getOne();
            String expected = pair.getTwo();
            String actual = getMassagedIp(testIp);
            System.out.println((IPUtil.isEqual(actual, expected) ? "SUCCESS: " : "ERROR: ") + "Actual and expected results; ip/" + testIp + " actual/" + actual + " expected/" + expected);
        }
    }

    public void terminateSession() {
        // bl: SessionMap keeps around an internal reference to the HttpSession.  since we just invalidated
        // the HttpSession, we should also invalidate the internal HttpSession on the SessionMap.  this way,
        // if we attempt to get an attribute from the SessionMap later on in the request (e.g. when getting
        // a UserSession object for logging purposes), the SessionMap will get a new HttpSession to work with
        // rather than using the old, now invalidated session (which will result in a "Session already invalidated"
        // exception being thrown).

        // bl: first attempt an invalidate on the SessionMap.  we'll try invalidating directly on the HttpSession
        // object on the HttpServletRequest down below in case they are different (which shouldn't ever be the case).
        ActionContext actionContext = ActionContext.getContext();
        if (actionContext != null) {
            // bl: can't use SessionMap.invalidate due to the ordering of operations.  it first calls invalidate
            // on the HttpSession before resetting its internal HttpSession reference to null.  then, in one of our
            // end of session event listeners (on session unbind) we do stuff and try to log it.  attempting to log
            // the changes results in checking if we have a UserSession object in the session.  that uses the SessionMap
            // which still has a reference to the now invalidated session, thus resulting in a "Session already invalidated"
            // error.  so, instead of using SessionMap.invalidate, just put a temporary HashMap as the Session so that
            // there will be no session objects in existence during the session invalidation.  after we have invalidated
            // the session, we can set the Session back to a normal SessionMap object.
            actionContext.setSession(new HashMap());
            /*Map sessionMap = actionContext.getSession();
            if(sessionMap!=null && sessionMap instanceof SessionMap) {
                ((SessionMap)sessionMap).invalidate();
            }*/
        }

        // note that above, we did not actually invalidate the HttpSession.  we just created a temporary map for the Session
        // whose lifecycle will last just during the invalidate call.
        try {
            HttpSession sess = request.getSession(false);
            if (sess != null) {
                // bl: invalidate is slightly more explicit regarding what we're trying to do here (though functionally,
                // I believe they will have the same behavior).
                sess.invalidate();
                //sess.setMaxInactiveInterval(0);
            }
        } finally {
            // set the Session map on the ActionContext back to a regular SessionMap.
            if (actionContext != null) {
                actionContext.setSession(new SessionMap(request));
            }
        }
    }

    public String getSessionId(boolean required) {
        HttpSession session = request.getSession(required);
        return session == null ? null : session.getId();
    }

    public void deleteCookie(String cookieName) {
        deleteCookie(cookieName, null, null);
    }

    public void deleteCookie(String cookieName, String domain, String path) {
        // bl: get the cookie from the request first to see if it actually needs to be deleted.
        // after that, however, create a new cookie if no pending cookie is found to ensure
        // we don't have any collisions if deleting cookies with the same name on separate paths in the same request.
        boolean hasRequestCookie = getCookieFromRequest(cookieName) != null;

        if (IPStringUtil.isEmpty(path)) {
            path = "/";
        }
        Cookie cookie = getPendingCookie(cookieName, path);
        if (cookie == null && !hasRequestCookie) {
            // nothing to do if no cookie supplied in the request.
            return;
        }

        // if there is a request cookie with this name, but no pending cookie with this name/path combo,
        // then create a new cookie to include in the response.
        if (cookie == null) {
            cookie = new Cookie(cookieName, "");
        }

        // bl: we used to set max age to 1.  that didn't seem to be deleting cookies.
        // setting to 0 now to be inline with the spec.
        //cookie.setMaxAge(1);
        cookie.setMaxAge(0);
        if (!IPStringUtil.isEmpty(domain)) {
            cookie.setDomain(domain);
        }
        cookie.setPath(path);
        // give the cookies an empty value as well.
        cookie.setValue("");
        addPendingCookie(cookie);
    }

    public void sendPendingCookies(boolean sslEnabled) {
        for (Cookie cookie : cookieNameAndPathToPendingCookie.values()) {
            cookie.setSecure(sslEnabled);
            response.addCookie(cookie);
        }
    }

    public void addDateHeader(String name, long date) {
        response.addDateHeader(name, date);
    }

    public void setExpiresHeaderToOneYear() {
        IPHTMLUtil.setExpiresHeaderToOneYear(response);
    }

    public void setStatus(int status) {
        response.setStatus(status);
    }

    public void addHeader(String name, String value) {
        response.addHeader(name, value);
    }

    public Map getParams() {
        if (dontAllowParamFetching) {
            throw UnexpectedError.getRuntimeException("Shouldn't getParams for requests that don't support it!");
        }
        return request.getParameterMap();
    }

    public boolean isDontAllowParamFetching() {
        return dontAllowParamFetching;
    }

    public ClientAgentInformation getClientAgentInformation() {
        if (clientAgentInformation == null) {
            clientAgentInformation = new ClientAgentInformation(request);
        }
        return clientAgentInformation;
    }

    public void setClientAgentInformationIfNecessary(ClientAgentInformation clientAgentInformation) {
        // bl: only set it if it's not already set.
        if (clientAgentInformation == null) {
            this.clientAgentInformation = clientAgentInformation;
        }
    }

    /**
     * get the MD5 key for a client request.  used for sharing sessions between "common"
     * clients that don't support cookies (e.g. search engine spiders).
     *
     * @return the MD5 key to use for sharing UserSessions among clients that don't support cookies.
     */
    public String getMd5KeyForClientRequest() {
        if (IPStringUtil.isEmpty(md5KeyForClientRequest)) {
            md5KeyForClientRequest = IPStringUtil.getMD5DigestFromString(getKeyForClientRequest());
        }
        return md5KeyForClientRequest;
    }

    /**
     * get the key for a client request.  this is the string that is MD5'd in order to get
     * the result for getMd5KeyForClientRequest().
     *
     * @return the key for this client request.  includes things like the user agent, ip address, and a few headers.
     */
    public String getKeyForClientRequest() {
        if (IPStringUtil.isEmpty(keyForClientRequest)) {
            StringBuilder uniqueKey = new StringBuilder();
            // bl: changing the key for client requests to be just the user-agent. incorporating the IP as well
            // makes the spiders page & map blow up.  this will help reduce the size of that.
            // nb. still using the StringBuilder so that null user agent strings will be displayed as the string "null".
            String userAgentString = getUserAgentString();
            // bl: use a lowercase user-agent string as part of the key
            if (userAgentString != null) {
                userAgentString = userAgentString.toLowerCase();
            }
            uniqueKey.append(userAgentString);
            keyForClientRequest = uniqueKey.toString();
        }
        return keyForClientRequest;
    }

    public String getUserAgentString() {
        return getHeader("user-agent");
    }

    @Override
    public <T> T getRequestAttribute(String name) {
        return (T) request.getAttribute(name);
    }

    @Override
    public void setRequestAttribute(String name, Object value) {
        request.setAttribute(name, value);
    }

    @Override
    public Map<String, String> getPrefixedParameters(String prefix, Set<String> paramNamesToExclude) {
        Map<String, String> params = newLinkedHashMap();

        for (String name : (Set<String>) getParams().keySet()) {
            if (name != null && name.startsWith(prefix) && (paramNamesToExclude == null || !paramNamesToExclude.contains(name.substring(prefix.length())))) {
                params.put(name, getParamValue(name));
            }
        }

        return params;
    }

    @Override
    public Map<String, String[]> getPrefixedParameterValues(String prefix, Set<String> paramNamesToExclude) {
        Map<String, String[]> params = newLinkedHashMap();

        for (String name : (Set<String>) getParams().keySet()) {
            if (name != null && name.startsWith(prefix) && (paramNamesToExclude == null || !paramNamesToExclude.contains(name.substring(prefix.length())))) {
                params.put(name, (String[]) getParams().get(name));
            }
        }

        return params;
    }

    private Boolean useBytesRange;
    private Integer contentLengthForParsing;
    private Integer rangeBytesStart;
    private Integer rangeBytesEnd;
    private Integer rangeContentLength;

    @Override
    public boolean isUseBytesRange() {
        assert useBytesRange != null : "Should only get usesBytesRange after parsing the range!";
        return useBytesRange;
    }

    @Override
    public boolean isPartialFileStream() {
        // bl: don't record stats when using the scrubber to jump around a video/audio file, as evidenced
        // by a non-zero start index in the bytes array. -1 indicates that we're scanning from a location to the end
        // of the file (as offset by the bytesEnd). any other positive value indicates the start position in the
        // file to start streaming from.
        // bl: in order to avoid exceptions, support invoking this method even when parseRange hasn't been called.
        return useBytesRange != null && isUseBytesRange() && getRangeBytesStart() != 0;
    }

    @Override
    public Integer getRangeBytesStart() {
        assert useBytesRange != null : "Should only get rangeBytesStart after parsing the range!";
        return rangeBytesStart;
    }

    @Override
    public Integer getRangeBytesEnd() {
        assert useBytesRange != null : "Should only get rangeBytesEnd after parsing the range!";
        return rangeBytesEnd;
    }

    @Override
    public Integer getRangeContentLength() {
        assert useBytesRange != null : "Should only get rangeContentLength after parsing the range!";
        return rangeContentLength;
    }

    @Override
    public void parseRange(int contentLength) {
        if (useBytesRange != null) {
            assert contentLengthForParsing == contentLength : "ContentLength mismatch between parse attempts! A bug will likely ensue! Initial contentLength/" + contentLengthForParsing + ". New contentLength/" + contentLength;
            return;
        }
        contentLengthForParsing = contentLength;
        String rangeHeader = request.getHeader("range");
        if (!isEmpty(rangeHeader) && rangeHeader.startsWith("bytes=")) {
            int sep = rangeHeader.indexOf("-");
            if (sep > 0) {
                // todo: make this support multiple ranges at some point - what would it be needed/used for?
                // bl: range headers can support multiple ranges.  it's easier to only support one here, so only honor the first.
                int commaIndex = rangeHeader.indexOf(',');
                if (commaIndex >= 0) {
                    rangeHeader = rangeHeader.substring(0, commaIndex);
                }

                String bytesStartStr = rangeHeader.substring(6, sep);
                rangeBytesStart = isEmpty(bytesStartStr) ? -1 : Integer.parseInt(bytesStartStr);
                String bytesEndStr = rangeHeader.substring(sep + 1);
                rangeBytesEnd = isEmpty(bytesEndStr) ? -1 : Integer.parseInt(bytesEndStr);

                // bl: if the bytesStartStr is empty, then treat as the last X bytes
                // refer: http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.35.1
                if (rangeBytesStart == -1) {
                    // if no start range was specified, then the "end" value represents that many bytes to return
                    // from the end of the response
                    rangeContentLength = Math.min(rangeBytesEnd, contentLength);
                    rangeBytesStart = Math.max(contentLength - rangeBytesEnd, 0);
                    // bl: since these range values are inclusive, we now need to send the end value to the contentLength minus 1
                    rangeBytesEnd = contentLength - 1;
                } else {
                    if (rangeBytesEnd == -1 || rangeBytesEnd > contentLength) {
                        rangeBytesEnd = contentLength - 1;
                    }
                    rangeContentLength = rangeBytesEnd - rangeBytesStart + 1;
                }

                useBytesRange = true;
                return;
            }
        }

        useBytesRange = false;
    }

    @Override
    public void setNoCacheHeaders() {
        // bl: used to only set no cache for ajax requests to prevent ajax GET request caching in IE.
        // now, there have still been some issues with page caching in browsers, so we're going to
        // be explicit about every GET request and make sure that the HTML is not cached.
        //if(MethodPropertiesUtil.isAjaxRequest(actionInvocation))

        // bl: note that this will set Cache-Control to no-cache, but it also sets the Expires header
        // to 0 and the Vary header to *.
        // protocol info: http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html
        IPHTMLUtil.setNoCache(request, response);
    }

    @Override
    public Object getRequestBodyObject() {
        return requestBodyObject;
    }

    @Override
    public void setRequestBodyObject(Object requestBodyObject) {
        this.requestBodyObject = requestBodyObject;
    }

    @Override
    public Map<String, List<String>> getMultipartFiles() {
        if(multipartRequest==null) {
            return null;
        }
        MultiValueMap<String, MultipartFile> fileMap = multipartRequest.getMultiFileMap();
        Map<String, List<String>> paramNameToFilenames = new LinkedHashMap<>();
        for (Map.Entry<String, List<MultipartFile>> entry : fileMap.entrySet()) {
            String paramName = entry.getKey();
            List<MultipartFile> multipartFiles = entry.getValue();
            List<String> filenames = new ArrayList<>(multipartFiles.size());
            for (MultipartFile multipartFile : multipartFiles) {
                filenames.add(multipartFile.getOriginalFilename());
            }
            paramNameToFilenames.put(paramName, filenames);
        }
        return paramNameToFilenames;
    }

    @Override
    public void setSpringMultipartRequest(MultipartHttpServletRequest multipartRequest) {
        this.multipartRequest = multipartRequest;
    }
}
