package org.narrative.common.util.html;

import org.narrative.common.util.posting.HtmlTextMassager;

/**
 * Created by IntelliJ IDEA.
 * User: paul
 * Date: Mar 15, 2005
 * Time: 12:49:26 PM
 * Removes all tags from an HTML docuemnt
 */
public class HTMLStripper extends HTMLParser {

    private final boolean stripEntities;

    private HTMLStripper(String html, HTMLParser.FragmentType fragmentType, boolean stripEntities) {
        super(html, fragmentType);
        this.stripEntities = stripEntities;
    }

    protected int startElement(String elementName, boolean isSelfClose) {
        // do nothing for the start of the element.  add the space on close.
        return 0;
    }

    protected int addAttribute(String attributeName, String attributeValue) {
        // do nothing for attributes
        return 0;
    }

    protected int endStartElement() {
        // do nothing until the element is closed
        return 0;
    }

    protected int closeElement(String elementName, boolean isSelfClose) {
        // append a space if the last character wasn't a space
        // bl: only for self-closing elements.  all other elements may actually contain data and so a space is not necessary.
        if (buffer.length() > 0 && isSelfClose) {
            return appendSpaceIfNecessary();
        }
        return 0;
    }

    protected int commentNode(String comment) {
        // do nothing for comments
        return 0;
    }

    protected int textNode(String text) {
        if (stripEntities) {
            text = HtmlTextMassager.stripEntities(text);
        }
        int ret = super.textNode(text);
        // bl: always append a space to the end of each text node to ensure that we won't "collapse" text contained
        // in HTML elements.
        ret += appendSpaceIfNecessary();
        return ret;
    }

    private int appendSpaceIfNecessary() {
        if (buffer.length() > 0) {
            if (buffer.charAt(buffer.length() - 1) != ' ') {
                buffer.append(' ');
                return 1;
            }
        }
        return 0;
    }

    public static String stripHtmlFragment(String textToStripHtmlFrom, boolean stripEntities) {
        return new HTMLStripper(textToStripHtmlFrom, HTMLParser.FragmentType.BODY, stripEntities).parse();
    }

    public static String stripBlockQuoteElement(String textToStripQuoteFrom) {
        HTMLParser htmlParser = new HTMLParser(textToStripQuoteFrom, HTMLParser.FragmentType.BODY);
        htmlParser.addElementToRemove("blockquote");
        return htmlParser.parse();
    }
}
