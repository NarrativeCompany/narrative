package org.narrative.common.util.html;

import org.narrative.network.shared.email.EmailCssClass;
import org.narrative.network.shared.email.EmailElementCss;

import java.util.Collections;
import java.util.Map;
import java.util.Stack;

import static org.narrative.common.util.CoreUtils.*;
import static org.narrative.network.shared.util.NetworkCoreUtils.*;

/**
 * Date: 11/13/12
 * Time: 3:48 PM
 *
 * @author brian
 */
public class HTMLEmailStyleParser extends HTMLParser {

    private static final String STYLE_ATTRIBUTE = "style";
    private static final String CLASS_ATTRIBUTE = "class";

    private static final Map<String, EmailCssClass> CLASS_NAME_TO_CSS;

    static {
        Map<String, EmailCssClass> map = newHashMap();
        map.put("noBorder", EmailCssClass.NO_BORDER);
        map.put("graemlin", EmailCssClass.NO_BORDER);
        map.put("quoteHeading", EmailCssClass.QUOTE_HEADER);
        map.put("quote-header", EmailCssClass.QUOTE_HEADER);
        map.put("biggest", EmailCssClass.BIGGEST_TEXT);
        map.put("bigger", EmailCssClass.BIGGER_TEXT);
        map.put("big", EmailCssClass.BIG_TEXT);
        map.put("normal", EmailCssClass.NORMAL_TEXT);
        map.put("smaller", EmailCssClass.SMALLER_TEXT);
        map.put("smallest", EmailCssClass.SMALLEST_TEXT);
        map.put("quotedText", EmailCssClass.QUOTED_TEXT);
        map.put("quote", EmailCssClass.QUOTED_TEXT);

        for (EmailCssClass emailCssClass : EmailCssClass.values()) {
            assert !map.containsKey(emailCssClass.getCssClass()) : "Duplicate EmailCssClass found for " + emailCssClass.getCssClass();
            map.put(emailCssClass.getCssClass(), emailCssClass);
        }

        CLASS_NAME_TO_CSS = Collections.unmodifiableMap(map);
    }

    private final Stack<Boolean> quotedTextElements = new Stack<Boolean>();
    private String currentElementCss;
    private int quotedTextDepth;

    public HTMLEmailStyleParser(String html) {
        super(html, FragmentType.BODY);
    }

    @Override
    protected int startElement(String elementName, boolean isSelfClose) {
        currentElementCss = EmailElementCss.getCssForElementName(elementName);

        // bl: we don't care what elements get pushed. we just need to track the overall depth
        // so that we can keep track of the quotedTextDepth properly.
        quotedTextElements.push(Boolean.FALSE);

        return super.startElement(elementName, isSelfClose);
    }

    @Override
    protected int addAttribute(String attributeName, String attributeValue) {
        if (STYLE_ATTRIBUTE.equalsIgnoreCase(attributeName)) {
            addCssForCurrentElement(attributeValue);

            // bl: don't add the style attribute now. add it at the end in endStartElement.
            return 0;
        }
        if (!isEmpty(attributeValue) && CLASS_ATTRIBUTE.equalsIgnoreCase(attributeName)) {
            // bl: parse out class names and check each class name for CSS that may need to be applied.
            for (String className : attributeValue.split(" ")) {
                EmailCssClass cssClass = CLASS_NAME_TO_CSS.get(className);

                if (cssClass == null) {
                    continue;
                }

                // jw: if this class is for quoted text, we need to determine if we should be alternating colors
                if (cssClass.isQuotedText()) {
                    // bl: alternate the quotedTextAlt CSS based on nesting level.
                    if ((quotedTextDepth % 2) == 1) {
                        cssClass = EmailCssClass.QUOTED_TEXT_ALT;
                    }
                    // bl: track all of the quotedText elements in the stack
                    quotedTextElements.pop();
                    quotedTextElements.push(Boolean.TRUE);
                    quotedTextDepth++;
                }
            }
        }
        return super.addAttribute(attributeName, attributeValue);
    }

    @Override
    protected int endStartElement() {
        int attributeLength = appendCssStyleAttribute();
        return super.endStartElement() + attributeLength;
    }

    @Override
    protected int closeElement(String elementName, boolean isSelfClose) {
        Boolean isQuotedText = quotedTextElements.pop();
        if (isQuotedText) {
            quotedTextDepth--;
        }
        // bl: self-close elements don't ever call endStartElement. thus, need to append CSS for those here.
        int attributeLength = isSelfClose ? appendCssStyleAttribute() : 0;
        return super.closeElement(elementName, isSelfClose) + attributeLength;
    }

    private int appendCssStyleAttribute() {
        if (!isEmpty(currentElementCss)) {
            return super.addAttribute(STYLE_ATTRIBUTE, currentElementCss);
        }
        return 0;
    }

    private void addCssForCurrentElement(String css) {
        if (isEmpty(currentElementCss)) {
            currentElementCss = css;
        } else {
            if (!currentElementCss.endsWith(";")) {
                currentElementCss += ";";
            }
            currentElementCss += css;
        }
    }

}
