package org.narrative.common.util.html;

import org.narrative.common.util.IPStringUtil;
import org.narrative.common.util.IPUtil;
import org.narrative.common.util.UnexpectedError;
import org.narrative.common.util.posting.FormattableImpl;
import org.narrative.common.util.posting.MessageTextMassager;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.tidy.DOMElementImpl;
import org.w3c.tidy.DOMNodeImpl;
import org.w3c.tidy.Tidy;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Created by IntelliJ IDEA.
 * User: Katoth
 * Date: Jan 27, 2005
 * Time: 4:04:56 AM
 * <p/>
 * The concept of this class is to create a generic JTidy Dom walker.
 * An application that parses a HTML stream and allows you to change its contents by walking
 * over the resulting DOM of the tidied HTML.
 */
public class HTMLParser {
    private static final Set<String> SELF_CLOSING_ELEMENTS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList("br", "hr", "img", "param", "embed", "source")));
    protected final StringBuilder buffer = new StringBuilder();
    private final String html;
    protected final Tidy tidy = new Tidy();
    private final FragmentType fragmentType;
    private boolean isFragmentOpen;
    private boolean containsVisibleContent = false;
    private final ArrayList<String> elementsToRemove = new ArrayList<>();
    /**
     * this parameter can be used to escape the current element being output.  will just
     * convert the less-thans and greater-thans to &lt; and &gt; accordingly.
     */
    protected boolean escapeCurrentElement = false;

    private static final String NBSP_STRING = "&nbsp;";
    private static final Pattern NBSP_PATTERN = Pattern.compile(NBSP_STRING, Pattern.LITERAL | Pattern.CASE_INSENSITIVE);
    /*private static final String  NBSP_REPLACEMENT = "nnbbsspp";
    private static final Pattern NNBBSSPP_PATTERN = Pattern.compile(NBSP_REPLACEMENT, Pattern.LITERAL);*/

    private static final String CLASS_ATTRIBUTE = "class";
    private static final String EMBEDLY_CARD_CLASS_NAME = "embedly-card";

    public HTMLParser(String html, FragmentType fragmentType) {
        this.html = html;
        this.fragmentType = fragmentType;
    }

    /**
     * Tag elements to remove from resulted DOM parse.
     *
     * @param elementToRemove
     */
    public void addElementToRemove(String elementToRemove) {
        elementsToRemove.add(elementToRemove);
    }

    /**
     * Returns true if we aren't in the part of the document generated to support html fragments.  If this returns
     * false then you should know that these elements and attributes don't really exist in your document
     *
     * @return Whether or not we are within the actual document
     */
    protected boolean isInRealDocument() {
        return fragmentType == null || isFragmentOpen;
    }

    public static boolean doesHtmlFragmentContainVisibleContent(String htmlFragment) {
        // bl: in order to ensure consistent behavior with our message body processing (UBB Code, etc.),
        // we need to first pass the HTML through our MessageTextMassager to ensure all UBB Code conversion takes place.
        // then, we can inspect that output for visible content.
        FormattableImpl formattable = new FormattableImpl(htmlFragment, true);
        new MessageTextMassager(formattable, Collections.emptyMap(), formattable.isAreAllBodyElementsSupported()).massageBody();
        HTMLParser parser = new HTMLParser(formattable.getBody(), FragmentType.BODY);
        parser.parse();
        return parser.containsVisibleContent;
    }

    public final String parse() {
        if (IPStringUtil.isEmpty(html)) {
            return html;
        }

        // only replace if we find markup (if we do it for all, it messes up internationalized chars)
        if (!html.contains("<")) {
            // bl: need to pass through the textNode function so that any classes overriding textNode (with functionality)
            // can apply as necessary.  otherwise, textNode is bypassed and certain things won't work (e.g. HTMLStripper
            // strips out entities from the textual output or HTMLTruncator truncates text to a specified length).

            // bl: small-ish hack to set the body as open so that the textNode contents will be output no matter what
            // (regardless of isHtmlFragment).
            isFragmentOpen = true;
            // bl: this will also update the containsVisibleContent flag
            textNode(html);
            //pm: need to return buffer instead of html, otherwise we just threw out what the textNode function did
            return buffer.toString();
        }

        String ret = html;
        if (fragmentType != null) {
            ret = new StringBuilder().append(fragmentType.getPrefix()).append(ret).append(fragmentType.getSuffix()).toString();
        }

        // replace &nbsp; with nnbbsspp
        // don't need to do this anymore, now that we're using Tidy correctly (and have a custom build of Tidy to pretty print)
        /*if(keepNbspEntities) {
            ret = NBSP_PATTERN.matcher(ret).replaceAll(NBSP_REPLACEMENT);
        }*/

        try {
            ret = parse(new ByteArrayInputStream(ret.getBytes(IPUtil.IANA_UTF8_ENCODING_NAME)));
        } catch (UnsupportedEncodingException e) {
            throw UnexpectedError.getRuntimeException("Failed getting bytes from html string due to UnsupportedEncodingExcpetion.  UTF-8 not working? ret/" + ret, e, true);
        }

        // replace nnbbsspp with &nbsp;
        /*if(keepNbspEntities) {
            return NNBBSSPP_PATTERN.matcher(ret).replaceAll(NBSP_STRING);
        }*/

        return ret;
    }

    private boolean shouldParse() {
        return fragmentType == null || isFragmentOpen;
    }

    /**
     * Takes a input stream of HTML and JTidy Parses it and
     * iterates over the resulting DOM so we can do special
     * processing on it using the abstract functions
     *
     * @param in the InputStream that contains the HTML to be tidied
     * @return the resulting parsed HTML String
     */
    private String parse(InputStream in) {

        // this will prevent certain numeric entities from being incorrectly converted
        // to their closest ascii equivalents.  e.g. &#8212; -> '-' 
        tidy.setAsciiChars(false);
        // no need to drop empty paragraph tags
        tidy.setDropEmptyParas(false);
        // bl: no need to trim empty elements
        // actually, not trimming some empty elements can cause the entire page to break.  for example,
        // a self-closing div tag will screw up the display of the entire page since it is interpreted
        // by the browser as a closing div tag.
        // bl: NOW, we are going to not trim empty elements. the effect of this was that empty paragraphs
        // would be converted to <br> elements. we don't want <br> elements! long-live empty paragraphs!
        // note that we are now whitelisting the list of supported self-closing elements, so we shouldn't
        // self-close any divs anymore (which was the reason we had originally turned this on).
        tidy.setTrimEmptyElements(false);
        tidy.setInputEncoding(IPUtil.IANA_UTF8_ENCODING_NAME);
        // bl: output encoding defaults to ASCII.  let's use UTF-8 instead.  this will ensure
        // that our output is valid UTF-8.
        tidy.setOutputEncoding(IPUtil.IANA_UTF8_ENCODING_NAME);
        tidy.setMakeClean(true);
        tidy.setQuiet(true);
        tidy.setShowWarnings(false);
        tidy.setWraplen(0);
        // bl: use XHTML output.  means lowercase tag names and attributes.  also means xml output.
        // bl: no longer going to use XHTML. instead, we'll use most of what XHTML was providing
        // with the exception of XHTML/XML output and quoting of ampersands (so that ampersands
        // can be output directly unescaped, which is needed for JavaScript at times). see org.w3c.tidy.Configuration#adjust() for more details.
        //tidy.setXHTML(true);
        tidy.setUpperCaseTags(false);
        tidy.setUpperCaseAttrs(false);
        tidy.setHideEndTags(false);
        // bl: in order to allow ampersands to be used literally in javascript (e.g. in a string), let's not quote them.
        tidy.setQuoteAmpersand(false);
        // bl: it doesn't appear in my testing that this actually impacts anything, but putting it here
        // for explicitness anyway. we don't want the contents of CDATA to be escaped.
        tidy.setEscapeCdata(false);

        parse(tidy.parseDOM(in, null));
        return buffer.toString();
    }

    /**
     * Iterates over a Node and triggers the above Dom events
     * on each element
     *
     * @param node the root node of the Dom that needs to be parsed
     */
    private void parse(Node node) {
        if (node == null) {
            return;
        }

        int type = node.getNodeType();
        switch (type) {
            case Node.DOCUMENT_NODE:
                openDocument();
                parse(((Document) node).getDocumentElement());
                break;

            case Node.ELEMENT_NODE:
                String elementName = node.getNodeName();

                // jw: if the fragment type does not allow this element then lets not bother processing it
                if (fragmentType != null && fragmentType.excludeElement(elementName) || elementsToRemove.contains(elementName)) {
                    break;
                }

                // if there is an image element, then this document does contain visible content.
                if (!containsVisibleContent) {
                    if (IPStringUtil.isStringEqualIgnoreCase(elementName, "img") || IPStringUtil.isStringEqualIgnoreCase(elementName, "object") || IPStringUtil.isStringEqualIgnoreCase(elementName, "embed") || IPStringUtil.isStringEqualIgnoreCase(elementName, "video") || IPStringUtil.isStringEqualIgnoreCase(elementName, "audio") || IPStringUtil.isStringEqualIgnoreCase(elementName, "iframe")) {
                        containsVisibleContent = true;
                    }
                }

                NodeList children = node.getChildNodes();

                // detect if this is an empty Element
                // bl: used to always self-close all elements that had no inner content.
                // this was very bad in certain circumstances since it meant that an empty div
                // or an empty iframe would self-close, which oftentimes breaks the display of the page.
                // to fix, we are now going to inspect the tidy node (using reflection for now) to determine
                // if the element is a START_END_TAG, which implies self-closing.
                boolean isSelfClosingElement;//= (children==null || children.getLength()==0);
                // bl: force br elements to be self-closing. for whatever reason, JTidy is adding a closing br tag like:
                // <br></br>
                // which causes the browser to interpret it as two separate BRs.
                if (SELF_CLOSING_ELEMENTS.contains(elementName.toLowerCase())) {
                    isSelfClosingElement = true;
                } else {
                    try {
                        DOMElementImpl element = (DOMElementImpl) node;
                        Field adapteeField = DOMNodeImpl.class.getDeclaredField("adaptee");
                        adapteeField.setAccessible(true);
                        org.w3c.tidy.Node tidyNode = (org.w3c.tidy.Node) adapteeField.get(element);
                        Field tidyTypeField = org.w3c.tidy.Node.class.getDeclaredField("type");
                        tidyTypeField.setAccessible(true);
                        short tidyNodeType = tidyTypeField.getShort(tidyNode);
                        isSelfClosingElement = tidyNodeType == org.w3c.tidy.Node.START_END_TAG;
                    } catch (Throwable t) {
                        throw UnexpectedError.getRuntimeException("Failed getting Tidy adaptee node type! Can't determine if self-closing node!", t);
                    }
                }
                startElement(elementName, isSelfClosingElement);

                // Add all the attributes to the HTML Node
                NamedNodeMap attrs = node.getAttributes();
                List<Node> orderedAttributes = new ArrayList<Node>();
                for (int i = 0; i < attrs.getLength(); i++) {
                    Node attribute = attrs.item(i);
                    orderedAttributes.add(attribute);
                }

                Collections.sort(orderedAttributes, new Comparator<Node>() {
                    @Override
                    public int compare(Node node1, Node node2) {
                        return node1.getNodeName().toLowerCase().compareTo(node2.getNodeName().toLowerCase());
                    }
                });

                for (Node orderedAttribute : orderedAttributes) {
                    String nodeName = orderedAttribute.getNodeName();
                    String nodeValue = orderedAttribute.getNodeValue();
                    if(!containsVisibleContent) {
                        if(CLASS_ATTRIBUTE.equalsIgnoreCase(nodeName) && EMBEDLY_CARD_CLASS_NAME.equals(nodeValue)) {
                            containsVisibleContent = true;
                        }
                    }
                    addAttribute(nodeName, nodeValue);
                }

                if (isSelfClosingElement) {
                    closeElement(elementName, true);
                    break;
                }

                endStartElement();

                boolean isFragmentTag = fragmentType != null && fragmentType.isRootElement(elementName);
                // We dont want to start outputing until we have processed the body
                if (isFragmentTag) {
                    isFragmentOpen = true;
                }

                // recursively parse any sub nodes of this node
            {
                int len = children.getLength();
                for (int i = 0; i < len; i++) {
                    parse(children.item(i));
                }
            }

            if (isFragmentTag) {
                isFragmentOpen = false;
            }
            // trigger the close element event
            closeElement(elementName, false);

            break;

            case Node.TEXT_NODE:
                // trigger the abstract Dom event on the text node
                // note: JTidy will unescape characters during the cleanup process
                //       so lets re-escape the greater than and less thans here
                //       This should be safe since a normal entity would have been
                //       turned into ELEMENT DOM Nodes
                // bl: this doesn't work, unfortunately.  a bug in Tidy is preventing
                // the output stream from being flushed, which is resulting in some (and a lot of times ALL)
                // of the characters being left out of the output stream. dumb!
                /*ByteArrayOutputStream textOut = new ByteArrayOutputStream(1024);
                tidy.pprint(node, textOut);
                String text = textOut.toString();
                if(!IPStringUtil.isEmpty(text)) {
                    textNode(text);
                }*/
                //String value = HtmlTextMassager.disableHtml(node.getNodeValue());
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                tidy.pprint(node, out);
                // bl: pretty printed value can just get the string from the output byte array since
                // we're using Tidy's default output encoding of ASCII, which means that special (multi-byte/UTF-8)
                // characters will be encoded in the output as entity references.
                // actually, not going to do that anymore.  instead, need to interpret the chars
                // as UTF-8 from the result of Tidy's pretty print.
                //String prettyPrintedValue = out.toString();
                String prettyPrintedValue;
                try {
                    // need to get the string as UTF-8 since we're using UTF-8 as the output
                    // encoding from Tidy.
                    prettyPrintedValue = out.toString(IPUtil.IANA_UTF8_ENCODING_NAME);
                } catch (UnsupportedEncodingException e) {
                    throw UnexpectedError.getRuntimeException("Failed getting output from Tidy as UTF-8.  Unsupported encoding?  how? out/" + out.toString(), e, true);
                }

                // if the bove values, which it is in my test trying to get the content of a title node, then lets get the node value
                if (IPStringUtil.isEmpty(prettyPrintedValue)) {
                    prettyPrintedValue = node.getNodeValue();
                }

                if (!IPStringUtil.isEmpty(prettyPrintedValue)) {
                    textNode(prettyPrintedValue);
                }
                /*if (!IPStringUtil.isEmpty(value)) {
                    textNode(value);
                    updateContainsVisibleContentForText(value);
                }*/
                break;

            case Node.COMMENT_NODE:
                /*ByteArrayOutputStream commentOut = new ByteArrayOutputStream(1024);
                tidy.pprint(node, commentOut);
                String comment = commentOut.toString();
                if(!IPStringUtil.isEmpty(comment)) {
                    commentNode(comment);
                }*/
                String comment = node.getNodeValue();
                if (!IPStringUtil.isEmpty(comment)) {
                    commentNode(comment);
                }
                break;
            case Node.CDATA_SECTION_NODE:
                // uncomment this at some point if we decide we want to support CDATA sections.
                /*cdataStart();
                cdataNode(node.getNodeValue());
                cdataEnd();*/
                break;

            /*case Node.ENTITY_NODE:
                String entityName = node.getNodeName();
                break;

            case Node.ENTITY_REFERENCE_NODE:
                String entityReference = node.getNodeName();
                break;*/
        }
    }

    /**
     * By overloading these methods developers
     * should have complete control over the output of the
     * parsed HTML
     */

    /**
     * By default lets just let the document node remain empty
     */
    protected void openDocument() {}

    protected int startElement(String elementName, boolean isSelfClose) {
        if (!shouldParse()) {
            return 0;
        }
        int startLength = buffer.length();
        if (escapeCurrentElement) {
            buffer.append("&lt;");
        } else {
            buffer.append("<");
        }
        buffer.append(elementName);
        return buffer.length() - startLength;
    }

    protected int addAttribute(String attributeName, String attributeValue) {
        if (!shouldParse()) {
            return 0;
        }
        int startLength = buffer.length();
        // bl: using XMLUtil instead so that the attribute value is properly escaped.
        // bl: actually, we should assume JTidy properly escapes the attribute already. this code
        // would result in ampersands being double-escaped. so, back to the basics here.
        //XMLUtil.addNameEqualsValueAttribute(buffer, attributeName, attributeValue, true /* for html */);
        buffer.append(" ");
        buffer.append(attributeName);
        buffer.append("=\"");
        buffer.append(attributeValue);
        buffer.append("\"");
        return buffer.length() - startLength;
    }

    protected int endStartElement() {
        if (!shouldParse()) {
            return 0;
        }
        int startLength = buffer.length();
        if (escapeCurrentElement) {
            buffer.append("&gt;");
        } else {
            buffer.append(">");
        }
        return buffer.length() - startLength;
    }

    protected int closeElement(String elementName, boolean isSelfClose) {
        if (!shouldParse()) {
            return 0;
        }
        int startLength = buffer.length();
        if (isSelfClose) {
            if (escapeCurrentElement) {
                buffer.append(" /&gt;");
            } else {
                // bl: for our self-closing elements, let's just close them without XML/XHTML format. don't need
                // that format for HTML5 now.
                buffer.append(">");
            }
        } else {
            if (escapeCurrentElement) {
                buffer.append("&lt;/");
                buffer.append(elementName);
                buffer.append("&gt;");
            } else {
                buffer.append("</");
                buffer.append(elementName);
                buffer.append('>');
            }
        }
        return buffer.length() - startLength;
    }

    /**
     * Persist the text to the buffer
     *
     * @param text The flat text contained within this node
     * @return the number of characters written
     */
    protected int textNode(String text) {
        if (!shouldParse()) {
            return 0;
        }
        int startLength = buffer.length();
        buffer.append(text);
        updateContainsVisibleContentForText(text);
        return buffer.length() - startLength;
    }

    /*protected int cdataStart() {
        if(!shouldParse()) {
            return 0;
        }
        buffer.append(XMLUtil.CDATA_OPEN);
        return XMLUtil.CDATA_OPEN.length();
    }

    protected int cdataEnd() {
        if(!shouldParse()) {
            return 0;
        }
        buffer.append(XMLUtil.CDATA_CLOSE);
        return XMLUtil.CDATA_CLOSE.length();
    }

    protected int cdataNode(String cdata) {
        if(!shouldParse()) {
            return 0;
        }
        int startLength = buffer.length();
        buffer.append(cdata);
        return buffer.length()-startLength;
    }*/

    protected int commentNode(String comment) {
        if (!shouldParse()) {
            return 0;
        }
        int startLength = buffer.length();
        buffer.append("<!--");
        buffer.append(comment);
        buffer.append("-->");
        return buffer.length() - startLength;
    }

    private void updateContainsVisibleContentForText(String text) {
        // if we already have found visible content, then just return.
        if (containsVisibleContent) {
            return;
        }
        // strip out all non-breaking spaces
        String testForVisibleContent = NBSP_PATTERN.matcher(text).replaceAll("");
        /*if(keepNbspEntities) {
            testForVisibleContent = NNBBSSPP_PATTERN.matcher(text).replaceAll("");
        } else {
            testForVisibleContent = NBSP_PATTERN.matcher(text).replaceAll("");
        }*/
        // trim the body
        testForVisibleContent = testForVisibleContent.trim();
        // record whether or not it contains visible content.
        containsVisibleContent = !IPStringUtil.isEmpty(testForVisibleContent);
    }

    public static enum FragmentType {
        HEAD("head", "<html><head><meta content=\"HTML Tidy, see www.w3.org\" name=\"generator\" /><title />", "</head><body></body></html>", "title"),
        BODY("body", "<html><head><meta content=\"HTML Tidy, see www.w3.org\" name=\"generator\" /><title /></head><body>", "</body></html>");

        private final String rootElementName;
        private final String prefix;
        private final String suffix;
        private final Collection<String> elementsToExclude;

        private FragmentType(String rootElementName, String prefix, String suffix, String... elementsToExclude) {
            this.rootElementName = rootElementName;
            this.prefix = prefix;
            this.suffix = suffix;
            this.elementsToExclude = Collections.unmodifiableList(Arrays.asList(elementsToExclude));
        }

        public String getPrefix() {
            return prefix;
        }

        public String getSuffix() {
            return suffix;
        }

        public boolean excludeElement(String elementName) {
            return elementsToExclude.contains(elementName);
        }

        private boolean isRootElement(String elementName) {
            return rootElementName.equals(elementName);
        }
    }
}
