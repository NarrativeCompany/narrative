package org.narrative.network.shared.email;

import org.narrative.network.core.user.AuthZoneMaster;

import java.util.Collections;
import java.util.Map;

import static org.narrative.common.util.CoreUtils.*;
import static org.narrative.network.shared.util.NetworkCoreUtils.*;

/**
 * Date: 10/31/12
 * Time: 1:56 PM
 * User: jonmark
 */
public enum EmailElementCss implements EmailCssProvider {
    BODY("font-family:'Lato',Helvetica, Arial, sans-serif;font-size:12px;line-height:20px;color:#333333;background-color:#fbfdff;text-align:center;margin-top:15px;"),
    H1("margin-top:0px; font-size:28px; line-height:32px;"),
    H2("font-size:24px; line-height:30px;"),
    H3("font-size:19px; line-height:24px;"),
    A("color: #40a9ff;text-decoration:none;"),
    // jw: adding max-width and height to try and make images more responsive
    IMG(true, "margin: 0 10px 10px 10px;max-width:100%;height:auto;"),
    DIV(""),
    P("padding-top: 0; padding-bottom: 0; margin-top: 0; margin-bottom: 17px;"),
    SPAN(""),
    BLOCKQUOTE("padding: 10px; margin: 10px 0 10px 40px; border: 1px solid #cccccc; background-color:#dfdfdf;"),
    TABLE("border-spacing:0; border-collapse:collapse;"),
    TR(""),
    TD(""),
    UL("padding-left: 12px; list-style-type: inherit; list-style-position: outside; margin: 12px 0 0 20px; list-style-type: disc;"),
    OL("padding-left: 12px; list-style-type: inherit; list-style-position: outside; margin: 12px 0 0 20px; list-style-type: decimal;"),
    LI("padding: 0 0 12px 0;");

    private final String css;
    private final String elementName;
    private final boolean isSelfClose;

    private static final Map<String, EmailElementCss> ELEMENT_NAME_TO_CSS;

    static {
        Map<String, EmailElementCss> elementToCss = newHashMap();
        for (EmailElementCss element : values()) {
            assert !elementToCss.containsKey(element.elementName) : "Should never register the same element name multiple times: " + element.elementName;

            elementToCss.put(element.elementName, element);
        }

        ELEMENT_NAME_TO_CSS = Collections.unmodifiableMap(elementToCss);
    }

    private EmailElementCss(String css) {
        this(false, css);
    }

    private EmailElementCss(boolean isForcedClosed, String css) {
        this.isSelfClose = isForcedClosed;
        this.css = css;
        this.elementName = name().toLowerCase();
    }

    @Override
    public String getCss() {
        return css;
    }

    public String getElementName() {
        return elementName;
    }

    public boolean isSelfClose() {
        return isSelfClose;
    }

    public boolean isBody() {
        return this == BODY;
    }

    public static EmailElementCss getEmailElementCssForElementName(String elementName) {
        return ELEMENT_NAME_TO_CSS.get(elementName.toLowerCase());
    }

    public static String getCssForElementName(String elementName) {
        EmailElementCss element = getEmailElementCssForElementName(elementName);

        if (element == null) {
            return null;
        }

        return element.getCss();
    }

    public static boolean isElementSelfClosed(String elementName) {
        EmailElementCss element = getEmailElementCssForElementName(elementName);

        return element != null && element.isSelfClose();
    }
}
