package org.narrative.common.util.posting;

import org.narrative.common.core.services.interceptors.BypassHtmlDisable;

/**
 * Date: Jun 23, 2006
 * Time: 11:42:13 AM
 *
 * @author Brian
 */
public class FormattableImpl implements Formattable {

    private String body;
    protected final boolean areAllBodyElementsSupported;

    public FormattableImpl() {
        this(false);
    }

    public FormattableImpl(boolean areAllBodyElementsSupported) {
        this.areAllBodyElementsSupported = areAllBodyElementsSupported;
    }

    public FormattableImpl(String body, boolean areAllBodyElementsSupported) {
        this(areAllBodyElementsSupported);
        this.body = body;
    }

    public String getBody() {
        return body;
    }

    @BypassHtmlDisable
    public void setBody(String body) {
        this.body = body;
    }

    public boolean isAreAllBodyElementsSupported() {
        return areAllBodyElementsSupported;
    }
}
