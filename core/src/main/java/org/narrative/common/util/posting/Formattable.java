package org.narrative.common.util.posting;

import org.narrative.common.core.services.interceptors.BypassHtmlDisable;

/**
 * Date: Oct 12, 2005
 * Time: 3:29:32 PM
 *
 * @author Brian
 */
public interface Formattable {
    public static final String BODY_PARAM = "body";

    public String getBody();

    @BypassHtmlDisable
    public void setBody(String body);
}
