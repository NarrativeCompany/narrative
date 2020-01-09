package org.narrative.common.util.html;

import org.narrative.common.util.IPStringUtil;

import java.util.Stack;

/**
 * Created by IntelliJ IDEA.
 * User: Katoth
 * Date: Jan 27, 2005
 * Time: 6:46:25 AM
 * Truncates a HTML string ensuring that all elements are properly opened and closed.
 */
public class HTMLTruncator extends HTMLParser {

    protected int remainingLength;
    protected int elementStartPos = -1;
    protected final boolean truncateToEndOfWord;
    private boolean isClosingAll = false;
    /**
     * Since we will stop outputing at a certain length lets just
     * create a simple way to track what nodes have been opened.
     * <p>
     * Its important to only close nodes that have been opened in the order
     * that they are opened.
     */
    private Stack<String> openElements = new Stack<String>();

    private HTMLTruncator(String html, HTMLParser.FragmentType fragmentType, boolean truncateToEndOfWord, int maxLength) {
        super(html, fragmentType);
        this.remainingLength = maxLength - 3;
        this.truncateToEndOfWord = truncateToEndOfWord;
    }

    /**
     * Add a self closing element to the output buffer if we have enough room for it.
     *
     * @param elementName The name of the element
     * @param isSelfClose Whether it is a self closing element
     */
    protected int startElement(String elementName, boolean isSelfClose) {
        if (remainingLength <= 0) {
            isClosingAll = true;
            return 0;
        }

        // keep track of where the element started in case the element goes
        // over the supported length.
        elementStartPos = buffer.length();
        openElements.push(elementName);

        int charsWritten = super.startElement(elementName, isSelfClose);
        // decrement the characters written for the start of the element
        remainingLength -= charsWritten;

        // also, decrement for the length of the closing element here.  note that
        // the length of the start element will be decremented in endStartElement().
        if (isInRealDocument()) {
            if (isSelfClose) {
                // " />"
                remainingLength -= 3;
            } else {
                // "</elementName>"
                remainingLength -= (elementName.length() + 3);
            }
        }
        return charsWritten;
    }

    protected int addAttribute(String attributeName, String attributeValue) {
        if (isClosingAll) {
            return 0;
        }
        int charsWritten = super.addAttribute(attributeName, attributeValue);
        remainingLength -= charsWritten;
        return charsWritten;
    }

    protected int endStartElement() {
        // bail out early if we didn't output the start element, as evidenced by elementStartPos<0.
        if (remainingLength <= 0 && elementStartPos < 0) {
            isClosingAll = true;
            return 0;
        }

        int charsWritten = super.endStartElement();
        // decrement the remaining length based on the end of the start element (should just be 1).
        remainingLength -= charsWritten;
        // if we went over our size, then strip the element back off, and we're pretty much done.
        if (remainingLength < 0) {
            isClosingAll = true;
            buffer.delete(elementStartPos, buffer.length());
            // remove the element from the stack, too
            openElements.pop();
        }
        // reset the element start position
        elementStartPos = -1;
        return charsWritten;
    }

    /**
     * Outputs the close element tag as long as this close
     * element isnt for a element that is deeper than we
     * had to stop at.
     *
     * @param elementName the name of the element that we are closing
     * @param isSelfClose true if this is a self closing element.
     */
    protected int closeElement(String elementName, boolean isSelfClose) {
        // always close the element if it is in the stack.
        // i.e. if we opened the element, we better close it!
        if (!openElements.empty() && !elementName.equals(openElements.peek())) {
            return 0;
        }

        openElements.pop();

        // nb. don't decrement remainingLength here since it was already addressed
        return super.closeElement(elementName, isSelfClose);
    }

    /**
     * Adds as much of this node without going over the quota
     *
     * @param text the text contents of the text node
     */
    protected int textNode(String text) {
        if (remainingLength <= 0) {
            isClosingAll = true;
            return 0;
        }

        // if we made it here we know we have more than 3 remaining characters
        // but do we need to truncate this text?
        if (text.length() > remainingLength) {
            String truncatedText;
            if (truncateToEndOfWord) {
                truncatedText = IPStringUtil.getStringTruncatedToEndOfWord(text, remainingLength + 3);
            } else {
                truncatedText = text.substring(0, remainingLength);
                truncatedText += "...";
            }

            remainingLength = 0;
            isClosingAll = true;
            return super.textNode(truncatedText);
        }

        int charsWritten = super.textNode(text);
        remainingLength -= charsWritten;
        return charsWritten;
    }

    public static String truncateHTML(String html, HTMLParser.FragmentType fragmentType, boolean truncateToEndOfWord, int length) {
        if (IPStringUtil.isEmpty(html)) {
            return html;
        }

        // nothing to truncate?
        if (html.length() < length) {
            return html;
        }

        return new HTMLTruncator(html, fragmentType, truncateToEndOfWord, length).parse();
    }
}
