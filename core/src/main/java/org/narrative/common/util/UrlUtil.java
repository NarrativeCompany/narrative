package org.narrative.common.util;

import org.narrative.common.util.posting.HtmlTextMassager;
import org.apache.commons.validator.routines.UrlValidator;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by IntelliJ IDEA.
 * User: barry
 * Date: Apr 19, 2006
 * Time: 10:39:49 AM
 */
public class UrlUtil {
    private static final UrlValidator URL_VALIDATOR = new UrlValidator(new String[]{"http", "https"}, UrlValidator.ALLOW_LOCAL_URLS);
    private static final UrlValidator URL_VALIDATOR_WITH_2_SLASHES = new UrlValidator(new String[]{"http", "https"}, UrlValidator.ALLOW_2_SLASHES | UrlValidator.ALLOW_LOCAL_URLS);

    public static boolean isUrlValid(String url) {
        return isUrlValid(url, false);
    }

    public static boolean isUrlValid(String url, boolean isAllow2Slashes) {
        UrlValidator urlValidator = isAllow2Slashes ? URL_VALIDATOR_WITH_2_SLASHES : URL_VALIDATOR;
        return urlValidator.isValid(url);
    }

    public static String getWellFormedUrl(String url) {
        return getWellFormedUrl(url, false);
    }

    public static String getWellFormedUrl(String url, boolean isAllow2Slashes) {
        // then, make sure the url is well-formed!
        if (isUrlValid(url, isAllow2Slashes)) {
            return url;
        }

        // not well formed?  then try a relative url.
        url = "http://" + url;
        if (isUrlValid(url, isAllow2Slashes)) {
            return url;
        }

        // still not a well-formed url?  must not be valid.
        return null;
    }

    public static String sanitizeUrl(String url) {
        if (IPStringUtil.isEmpty(url)) {
            return url;
        }
        // first, sanitize the url and make sure it doesn't have anything "bad" in it.
        // bl: no need to disable HTML since we don't want & to turn into &amp;
        url = HtmlTextMassager.sanitizePlainTextString(url, false);
        // bl: we do need to disable less than and greater thans, though.  just not ampersands.
        url = HtmlTextMassager.disableLessThanAndGreaterThan(url);
        // can't contain javascript:
        if (url.contains("javascript:")) {
            return null;
        }

        return getWellFormedUrl(url);
    }

    public static String getDomainFromUrl(String url) {
        if (!isUrlValid(url)) {
            return null;
        }
        try {
            URL urlObj = new URL(url);
            return urlObj.getHost();
        } catch (MalformedURLException e) {
            return null;
        }
    }

    public static String getPathFromUrl(String url) {
        try {
            URL urlObj = new URL(url);
            return urlObj.getPath();
        } catch (MalformedURLException e) {
            return null;
        }
    }

    public static String getBaseUrl(String url) {
        if (IPStringUtil.isEmpty(url)) {
            return url;
        }
        try {
            URL urlObj = new URL(url);
            return urlObj.getProtocol() + "://" + urlObj.getHost();
        } catch (MalformedURLException e) {
            return null;
        }
    }
}
