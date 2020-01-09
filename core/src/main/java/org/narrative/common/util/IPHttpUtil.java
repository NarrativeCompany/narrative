package org.narrative.common.util;

import org.narrative.common.web.HttpMethodType;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.methods.StringRequestEntity;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.UnsupportedEncodingException;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Date: Jan 6, 2006
 * Time: 7:15:55 AM
 *
 * @author Brian
 */
public class IPHttpUtil {
    /**
     * get the "Referer" header.  note that it's spelled wrong.
     * don't know who the morons are who let that get into
     * the HTTP spec.
     *
     * @return the referrer for this request.  may be null or empty if there's no referrer.
     */
    public static String getReferrerHeader(HttpServletRequest request) {
        return request.getHeader("Referer");
    }

    /**
     * get the "User-Agent" header.
     *
     * @param request the request from which to extract the User-Agent string
     * @return the User-Agent string for the given request
     */
    public static String getUserAgent(HttpServletRequest request) {
        return request.getHeader("User-Agent");
    }

    public static boolean isPost(HttpServletRequest request) {
        return isMethodPost(request.getMethod());
    }

    public static boolean isMethodPost(String method) {
        return isMethodOfType(method, HttpMethodType.POST);
    }

    public static boolean isGet(HttpServletRequest request) {
        return isMethodGet(request.getMethod());
    }

    public static boolean isMethodGet(String method) {
        return isMethodOfType(method, HttpMethodType.GET);
    }

    public static boolean isHead(HttpServletRequest request) {
        return isMethodHead(request.getMethod());
    }

    public static boolean isMethodHead(String method) {
        return isMethodOfType(method, HttpMethodType.HEAD);
    }

    public static boolean isMethodOfType(String method, HttpMethodType methodType) {
        return methodType.toString().equalsIgnoreCase(method);
    }

    public static void sendPermanentRedirect(HttpServletResponse httpServletResponse, String redirect) {
        httpServletResponse.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
        httpServletResponse.setHeader("Location", redirect);
        httpServletResponse.setHeader("Connection", "close");
    }

    public static String parseNextLinkFromLinkHeader(Header header) {
        if (header == null) {
            return null;
        }
        for (String linkValue : header.getValue().split("\\,")) {
            if (isEmpty(linkValue)) {
                continue;
            }
            linkValue = linkValue.trim();
            if (linkValue.contains("rel=\"next\"")) {
                return parseLinkFromLinkHeader(linkValue);
            }
        }
        return null;
    }

    /**
     * parse a link header response value
     * expected format like:
     * <https://api.recurly.com/v2/coupons?cursor=1304958672>; rel="next"
     *
     * @param linkHeaderValue
     */
    public static String parseLinkFromLinkHeader(String linkHeaderValue) {
        int semiColonIndex = linkHeaderValue.indexOf(';');
        if (semiColonIndex >= 0) {
            linkHeaderValue = linkHeaderValue.substring(0, semiColonIndex);
        }
        if (linkHeaderValue.startsWith("<")) {
            linkHeaderValue = linkHeaderValue.substring(1);
        }
        if (linkHeaderValue.endsWith(">")) {
            linkHeaderValue = linkHeaderValue.substring(0, linkHeaderValue.length() - 1);
        }
        return linkHeaderValue;
    }

    public static final String CONTENT_DISPOSITION_INLINE = "inline";
    private static final String CONTENT_DISPOSITION_FILENAME_PARAMETER = "filename";

    public static String getFilenameContentDisposition(String filename) {
        StringBuilder sb = new StringBuilder(CONTENT_DISPOSITION_FILENAME_PARAMETER);
        sb.append("=\"");
        sb.append(filename);
        sb.append('"');
        return sb.toString();
    }

    public static final String CONTENT_DISPOSITION_ATTACHMENT_PREFIX = "attachment;";

    public static String getFileDownloadContentDisposition(String filename) {
        StringBuilder sb = new StringBuilder(CONTENT_DISPOSITION_ATTACHMENT_PREFIX);
        sb.append(getFilenameContentDisposition(filename));
        return sb.toString();
    }

    public static StringRequestEntity stringRequestEntityCreator(String body) {
        try {
            return new StringRequestEntity(XMLUtil.getXMLPrefix(null, IPUtil.IANA_UTF8_ENCODING_NAME) + body, "application/xml", IPUtil.IANA_UTF8_ENCODING_NAME);
        } catch (UnsupportedEncodingException e) {
            throw UnexpectedError.getRuntimeException("Could not create StringRequestEntity.", e);
        }
    }
}
