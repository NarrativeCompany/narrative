package org.narrative.common.web;

/**
 * Date: Mar 6, 2006
 * Time: 9:28:53 AM
 *
 * @author Brian
 */
public enum HttpMethodType {
    GET,
    HEAD,
    POST,
    /**
     * if a given action/method pair can support either a GET request or a POST request,
     * then use this constant to indicate that.  no GET/POST validation will occur for that
     * request.  instead, the action itself must do all of the necessary validation.
     */
    GET_OR_POST,
    UNSPECIFIED;
    // could add others like PUT, DELETE, etc.

    public boolean isGet() {
        return this == GET;
    }

    public boolean isHead() {
        return this == HEAD;
    }

    public boolean isPost() {
        return this == POST;
    }

    public boolean isGetOrPost() {
        return this == GET_OR_POST;
    }

    public boolean isUnspecified() {
        return this == UNSPECIFIED;
    }
}
