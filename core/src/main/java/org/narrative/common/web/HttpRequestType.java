package org.narrative.common.web;

/**
 * Date: 4/4/14
 * Time: 9:06 AM
 *
 * @author brian
 */
public enum HttpRequestType {
    STANDARD,
    AJAX,
    STANDARD_OR_AJAX;

    public boolean isStandard() {
        return this == STANDARD;
    }

    public boolean isAjax() {
        return this == AJAX;
    }

    public boolean isStandardOrAjax() {
        return this == STANDARD_OR_AJAX;
    }
}
