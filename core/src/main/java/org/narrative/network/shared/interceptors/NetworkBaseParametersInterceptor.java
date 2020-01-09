package org.narrative.network.shared.interceptors;

import com.opensymphony.xwork2.interceptor.ParametersInterceptor;

/**
 * Date: Feb 9, 2006
 * Time: 12:26:04 PM
 *
 * @author Brian
 */
public class NetworkBaseParametersInterceptor extends ParametersInterceptor {
    protected final boolean acceptableName(String name) {
        if (!super.acceptableName(name)) {
            return false;
        }

        // bl: i'm paranoid.  don't allow opening curly braces or parentheses in
        // our parameter names.  those characters have meaning in ognl, and I
        // don't want to have to worry about them.
        if (name.contains("{") || name.contains("(")) {
            return false;
        }

        return true;
    }
}
