package org.narrative.common.web;

import org.narrative.common.util.ClientAgentInformation;
import org.narrative.common.util.IPHttpUtil;
import org.narrative.common.util.IPStringUtil;
import org.narrative.common.util.posting.HtmlTextMassager;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.Cookie;

import java.util.Enumeration;
import java.util.Iterator;
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
 * Time: 10:51:50 AM
 * This interface is designed to allow the app to get and provide information about the
 * request and response without tying it specifically to the HttpServlet API.  This will be implemented
 * by HttpServletRequestResponseHandler and a test version to be created.
 */
public abstract class RequestResponseHandler {
    public static final String IS_SECURE_HEADER = "Is-Secure";
    public static final String FORWARDED_PROTOCOL_HEADER = "X-Forwarded-Proto";

    public abstract boolean isRawRequestBody();

    public abstract String getRequestBody();

    public abstract Cookie[] getCookies();

    public abstract void addPendingCookie(Cookie cookie);

    public abstract String getContentType();

    public abstract Enumeration getHeaderNames();

    public abstract String getHeader(String name);

    public abstract long getDateHeader(String name);

//    public abstract UserSession getUserSession();
//
//    public abstract UserSession createUserSession(Role role);

    public abstract Cookie getCookieFromRequest(String name);

    public abstract String getCookieValueFromRequest(String name);

    public abstract Cookie getPendingCookie(String name, String path);

    /**
     * check if this is a secure request (https)
     *
     * @return true if this is a secure (https) request.  false if it is not.
     */
    public abstract boolean isSecureRequest();

    public abstract boolean isRequestedAsAjax();

    public abstract String getMethod();

    public abstract String getPath();

    public abstract String getPathAndQuery();

    public abstract String getURI();

    public abstract String getHost();

    public abstract String getQueryString();

    public abstract String getUrl();

    public abstract String getBaseUrl();

    public abstract String getReferrer();

    public abstract String getScheme();

    public abstract String getRemoteHostIp();

    public abstract String getUnmassagedRemoteHostIp();

    public abstract Locale getLocale();

    public abstract ClientAgentInformation getClientAgentInformation();

    public abstract void setClientAgentInformationIfNecessary(ClientAgentInformation clientAgentInformation);

    /**
     * get the MD5 key for a client request.  used for sharing sessions between "common"
     * clients that don't support cookies (e.g. search engine spiders).
     *
     * @return the MD5 key to use for sharing UserSessions among clients that don't support cookies.
     */
    public abstract String getMd5KeyForClientRequest();

    /**
     * get the key for a client request.  this is the string that is MD5'd in order to get
     * the result for getMd5KeyForClientRequest().
     *
     * @return the key for this client request.  includes things like the user agent, ip address, and a few headers.
     */
    public abstract String getKeyForClientRequest();

    public abstract String getUserAgentString();

    public abstract void terminateSession();

    public abstract String getSessionId(boolean required);

    public abstract void deleteCookie(String cookieName, String domain, String path);

    public abstract void deleteCookie(String cookieName);

    public abstract void sendPendingCookies(boolean sslEnabled);

    public boolean isPost() {
        return IPHttpUtil.isMethodPost(getMethod());
    }

    public boolean isHead() {
        return IPHttpUtil.isMethodHead(getMethod());
    }

    public abstract void addDateHeader(String name, long date);

    public abstract void addHeader(String name, String value);

    public abstract void setExpiresHeaderToOneYear();

    public abstract <T> T getRequestAttribute(String name);

    public abstract void setRequestAttribute(String name, Object value);

    public abstract Map getParams();

    public abstract boolean isDontAllowParamFetching();

    public abstract Map<String, List<String>> getMultipartFiles();

    public abstract void setSpringMultipartRequest(MultipartHttpServletRequest multipartRequest);

    /**
     * get a parameter value for a query parameter.  will not return values for path parameters.
     *
     * @param paramName the parameter to get the single string value for
     * @return the first string value supplied for the given parameter name
     */
    public String getParamValue(String paramName) {
        String[] values = (String[]) getParams().get(paramName);
        if (isEmptyOrNull(values)) {
            return null;
        }
        return values[0];
    }

    public abstract void setStatus(int status);

    private static final Pattern NEWLINE_PATTERN = Pattern.compile("\\n");

    public static String getParametersString(Map map) {
        StringBuilder mapString = new StringBuilder();

        for (Iterator i = map.keySet().iterator(); i.hasNext(); ) {
            Object o = i.next();
            String parameterName = o.toString();
            mapString.append(parameterName).append("=");
            boolean hideParamValue = parameterName != null && (parameterName.toLowerCase().contains("password") || parameterName.toLowerCase().contains("secret"));

            Object value = map.get(o);
            mapString.append("{");
            if (value instanceof String[]) {
                String[] valueArray = (String[]) value;
                for (int j = 0; j < valueArray.length; j++) {
                    String paramValue = valueArray[j];
                    String paramValueToDisplay;
                    if (hideParamValue) {
                        paramValueToDisplay = "****";
                    } else {
                        // bl: truncate to 100 chars and convert newlines to display as \n instead of inserting
                        // a newline in the string.
                        paramValueToDisplay = IPStringUtil.getTruncatedString(paramValue, 100);
                        paramValueToDisplay = HtmlTextMassager.convertNewlineSequencesToCarriageReturns(paramValueToDisplay);
                        paramValueToDisplay = NEWLINE_PATTERN.matcher(paramValueToDisplay).replaceAll(Matcher.quoteReplacement("\\n"));
                    }
                    mapString.append("\"").append(paramValueToDisplay).append("\"");
                    if (j < valueArray.length - 1) {
                        mapString.append(",");
                    }
                }
            } else {
                mapString.append(value);
            }
            mapString.append("}");
            if (i.hasNext()) {
                mapString.append(",");
            }
        }
        return mapString.toString();
    }

    public String getReferrerExcludingCurrentUrl() {
        String referrer = getReferrer();

        // bl: instead of just doing a pure equality check, let's check to see if the current URL starts with
        // the referrer. this may be the case if the current URL has a confirmation message process oid in it.
        // in that case, we still don't want to do the redirect in order to avoid infinite redirects.
        if (isEmpty(referrer) || !getUrl().startsWith(referrer)) {
            return referrer;
        }

        return null;
    }

    public abstract Map<String, String> getPrefixedParameters(String prefix, Set<String> valuesToExclude);

    public abstract Map<String, String[]> getPrefixedParameterValues(String prefix, Set<String> valuesToExclude);

    public abstract void parseRange(int contentLength);

    public abstract boolean isUseBytesRange();

    public abstract boolean isPartialFileStream();

    public abstract Integer getRangeBytesStart();

    public abstract Integer getRangeBytesEnd();

    public abstract Integer getRangeContentLength();

    public abstract void setNoCacheHeaders();

    public abstract Object getRequestBodyObject();
    public abstract void setRequestBodyObject(Object requestBodyObject);
}