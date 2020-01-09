package org.narrative.common.util.posting;

import org.narrative.common.persistence.NameValuePair;
import org.narrative.common.util.Debug;
import org.narrative.common.util.IPStringUtil;
import org.narrative.common.util.html.HTMLEnforcer;
import org.narrative.common.util.html.HTMLParser;
import org.narrative.common.util.html.HTMLStripper;
import org.narrative.network.core.master.graemlins.Graemlin;
import org.narrative.network.core.master.graemlins.GraemlinType;
import org.apache.commons.lang3.StringEscapeUtils;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Date: Feb 22, 2006
 * Time: 12:26:18 PM
 *
 * @author Brian
 */
public class HtmlTextMassager {
    private static final String PARAGRAPH_OPEN = "<p>";
    private static final String PARAGRAPH_CLOSE = "</p>";
    private static final Pattern PARAGRAPH_TAG_PATTERN = Pattern.compile(PARAGRAPH_OPEN, Pattern.CASE_INSENSITIVE);
    private static final Pattern SELF_CLOSING_PARAGRAPH_TAG_PATTERN = Pattern.compile("<p/>", Pattern.CASE_INSENSITIVE);
    private static final Pattern CLOSING_PARAGRAPH_TAG_PATTERN = Pattern.compile(PARAGRAPH_CLOSE, Pattern.CASE_INSENSITIVE);
    private static final Pattern BR_TAG_PATTERN = Pattern.compile("<br\\ ?/?>", Pattern.CASE_INSENSITIVE);
    private static final String BR_REPLACEMENT = "<br />";

    private static final String NEWLINE_P_REPLACEMENT = PARAGRAPH_CLOSE + PARAGRAPH_OPEN;
    private static final Pattern PARAGRAPH_TAG_NEWLINE_PATTERN = Pattern.compile(NEWLINE_P_REPLACEMENT, Pattern.LITERAL | Pattern.CASE_INSENSITIVE);

    public static String convertBrAndParagraphTagsToCrLf(String message) {
        if (IPStringUtil.isEmpty(message)) {
            return message;
        }
        message = PARAGRAPH_TAG_PATTERN.matcher(message).replaceAll("");
        message = SELF_CLOSING_PARAGRAPH_TAG_PATTERN.matcher(message).replaceAll("\r\n");
        message = CLOSING_PARAGRAPH_TAG_PATTERN.matcher(message).replaceAll("\r\n\r\n");
        message = BR_TAG_PATTERN.matcher(message).replaceAll("\r\n");
        return message;
    }

    public static String convertParagraphTagsToCrLf(String message) {
        if (IPStringUtil.isEmpty(message)) {
            return message;
        }
        // bl: first, replace our closing + opening paragraph tag sequences
        message = PARAGRAPH_TAG_NEWLINE_PATTERN.matcher(message).replaceAll("\r\n");
        // bl: then, we just need to strip out any remaining opening and closing paragraph tags (which should really
        // only be at the start and the end of the input)
        message = PARAGRAPH_TAG_PATTERN.matcher(message).replaceAll("");
        message = CLOSING_PARAGRAPH_TAG_PATTERN.matcher(message).replaceAll("");
        return message;
    }

    public static final String STUB_PARAGRAPH_HTML = PARAGRAPH_OPEN + "<br>" + PARAGRAPH_CLOSE;

    private static final Pattern BR_TAG_SOF_PATTERN = Pattern.compile("\\A(<br\\ ?/?>)+", Pattern.CASE_INSENSITIVE);
    private static final Pattern NBSP_TAG_SOF_PATTERN = Pattern.compile("\\A(\\&nbsp;)+", Pattern.CASE_INSENSITIVE);
    private static final Pattern NBSP_160_TAG_SOF_PATTERN = Pattern.compile("\\A(\\&\\#160;)+", Pattern.CASE_INSENSITIVE);
    private static final Pattern BR_TAG_EOF_PATTERN = Pattern.compile("(<br\\ ?/?>)+\\z", Pattern.CASE_INSENSITIVE);
    private static final Pattern NBSP_TAG_EOF_PATTERN = Pattern.compile("(\\&nbsp;)+\\z", Pattern.CASE_INSENSITIVE);
    private static final Pattern NBSP_160_TAG_EOF_PATTERN = Pattern.compile("(\\&\\#160;)+\\z", Pattern.CASE_INSENSITIVE);
    // jw: The editor we are using for Narrative now places stub paragraphs after images when attached to ensure that the author
    //     has a place to place their cursor after the post.
    private static final Pattern STUB_PARAGRAPH_SOF_PATTERN = Pattern.compile("\\A(<p>(<br\\ ?/?>)*</p>)+", Pattern.CASE_INSENSITIVE);
    private static final Pattern STUB_PARAGRAPH_EOF_PATTERN = Pattern.compile("(<p>(<br\\ ?/?>)*</p>)+\\z", Pattern.CASE_INSENSITIVE);

    public static String trimHtmlWhitespace(String message) {
        if (IPStringUtil.isEmpty(message)) {
            return message;
        }
        message = BR_TAG_SOF_PATTERN.matcher(message).replaceAll("");
        message = NBSP_TAG_SOF_PATTERN.matcher(message).replaceAll("");
        message = NBSP_160_TAG_SOF_PATTERN.matcher(message).replaceAll("");
        message = BR_TAG_EOF_PATTERN.matcher(message).replaceAll("");
        message = NBSP_TAG_EOF_PATTERN.matcher(message).replaceAll("");
        message = NBSP_160_TAG_EOF_PATTERN.matcher(message).replaceAll("");
        message = STUB_PARAGRAPH_SOF_PATTERN.matcher(message).replaceAll("");
        message = STUB_PARAGRAPH_EOF_PATTERN.matcher(message).replaceAll("");
        return message;
    }

    private static final Pattern CONSECUTIVE_SPACES_PATTERN = Pattern.compile("\\ {2,}");
    private static final String SPACE_REPLACEMENT = " ";
    private static final String NBSP_REPLACEMENT = "&nbsp;";
    private static final Pattern NBSP_PATTERN = Pattern.compile(NBSP_REPLACEMENT, Pattern.LITERAL | Pattern.CASE_INSENSITIVE);

    private static final int MAX_CONSECUTIVE_SPACES = 10;

    public static String convertConsecutiveSpacesToNbsp(String message) {
        if (IPStringUtil.isEmpty(message)) {
            return message;
        }
        StringBuffer sb = new StringBuffer();
        Matcher matcher = CONSECUTIVE_SPACES_PATTERN.matcher(message);
        while (matcher.find()) {
            String spaces = matcher.group();
            // bl: allow at most 10 consecutive &nbsp; characters.
            int spacesToReplace = Math.min(spaces.length(), MAX_CONSECUTIVE_SPACES);
            // use the &nbsp; followed by one ASCII space
            StringBuilder newSpaces = new StringBuilder();
            for (int i = 1; i < spacesToReplace; i++) {
                newSpaces.append(NBSP_REPLACEMENT);
            }
            // bl: putting the space replacement _after_ all of the &nbsp; entities to try to avoid the scenario where
            // we have a leading space to start a wrapped line of text.
            newSpaces.append(SPACE_REPLACEMENT);
            matcher.appendReplacement(sb, newSpaces.toString());
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    public static String convertConsecutiveSpacesToSingleSpace(String message) {
        if (IPStringUtil.isEmpty(message)) {
            return message;
        }
        StringBuffer sb = new StringBuffer();
        Matcher matcher = CONSECUTIVE_SPACES_PATTERN.matcher(message);
        while (matcher.find()) {
            matcher.appendReplacement(sb, " ");
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    public static String convertNbspToSpace(String message) {
        if (IPStringUtil.isEmpty(message)) {
            return message;
        }
        message = NBSP_PATTERN.matcher(message).replaceAll(SPACE_REPLACEMENT);
        return message;
    }

    private static final Pattern TOO_MANY_CONSECUTIVE_NBSP_PATTERN = Pattern.compile("(\\&nbsp\\;){" + MAX_CONSECUTIVE_SPACES + ",}");
    private static final String TOO_MANY_CONSECUTIVE_NBSP_REPLACEMENT = "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";

    public static String stripLongConsecutiveNbspStrings(String message) {
        if (IPStringUtil.isEmpty(message)) {
            return message;
        }
        message = TOO_MANY_CONSECUTIVE_NBSP_PATTERN.matcher(message).replaceAll(TOO_MANY_CONSECUTIVE_NBSP_REPLACEMENT);
        return message;
    }

    public static String removeHtmlReturns(String message) {
        if (IPStringUtil.isEmpty(message)) {
            return message;
        }
        /*
        s/<P>/ /gi
        s/<P\/>/ /gi
        s/<BR>/ /gi
        s/<BR\/>/ /gi
        */
        message = PARAGRAPH_TAG_PATTERN.matcher(message).replaceAll(" ");
        message = SELF_CLOSING_PARAGRAPH_TAG_PATTERN.matcher(message).replaceAll(" ");
        message = CLOSING_PARAGRAPH_TAG_PATTERN.matcher(message).replaceAll(" ");
        message = BR_TAG_PATTERN.matcher(message).replaceAll(" ");
        return message;
    }

    // s/\r\n/<BR>/g
    private static final Pattern RN_RETURN_PATTERN = Pattern.compile("\r\n", Pattern.LITERAL);

    // s/\n/<BR>/g
    private static final Pattern N_RETURN_PATTERN = Pattern.compile("\n", Pattern.LITERAL);

    // s/\r/<BR>/g
    private static final Pattern R_RETURN_PATTERN = Pattern.compile("\r", Pattern.LITERAL);

    public static String convertCrAndLfToHtml(String message) {
        if (IPStringUtil.isEmpty(message)) {
            return message;
        }
        /*
        bl: i opted not to bring these over because they seem to arbitrary:
        s/(<BR>){4,}/<BR><BR><BR><BR>/g
        s/<!/< !/g
        */
        message = RN_RETURN_PATTERN.matcher(message).replaceAll(BR_REPLACEMENT);
        message = N_RETURN_PATTERN.matcher(message).replaceAll(BR_REPLACEMENT);
        message = R_RETURN_PATTERN.matcher(message).replaceAll(BR_REPLACEMENT);
        return message;
    }

    public static String convertCrAndLfToParagraphs(String message) {
        if (IPStringUtil.isEmpty(message)) {
            return message;
        }
        // bl: a little wonky, but wrap the entire thing in a paragraph, and we'll convert newlines
        // in the middle to paragraphs
        message = PARAGRAPH_OPEN + message + PARAGRAPH_CLOSE;
        message = RN_RETURN_PATTERN.matcher(message).replaceAll(NEWLINE_P_REPLACEMENT);
        message = N_RETURN_PATTERN.matcher(message).replaceAll(NEWLINE_P_REPLACEMENT);
        message = R_RETURN_PATTERN.matcher(message).replaceAll(NEWLINE_P_REPLACEMENT);
        return message;
    }

    public static String convertCrAndLfToSpaces(String message) {
        if (IPStringUtil.isEmpty(message)) {
            return message;
        }

        message = RN_RETURN_PATTERN.matcher(message).replaceAll(" ");
        message = N_RETURN_PATTERN.matcher(message).replaceAll(" ");
        message = R_RETURN_PATTERN.matcher(message).replaceAll(" ");
        return message;
    }

    /**
     * convert the following:
     * \r\n -> \n
     * \r -> \n
     * this way, all new lines can be represented with just \n
     *
     * @param message the message to convert
     * @return the message after converting all newline sequences to \n
     */
    public static String convertNewlineSequencesToCarriageReturns(String message) {
        if (IPStringUtil.isEmpty(message)) {
            return message;
        }
        message = RN_RETURN_PATTERN.matcher(message).replaceAll("\n");
        message = R_RETURN_PATTERN.matcher(message).replaceAll("\n");
        return message;
    }

    public static String stripNewlines(String message) {
        if (IPStringUtil.isEmpty(message)) {
            return message;
        }
        message = convertNewlineSequencesToCarriageReturns(message);
        message = N_RETURN_PATTERN.matcher(message).replaceAll("");
        return message;
    }

    private static final String AMPERSAND = "&";
    private static final String LESS_THAN = "<";
    private static final String GREATER_THAN = ">";
    private static final String AMPERSAND_ENTITY = "&amp;";
    private static final String LESS_THAN_ENTITY = "&lt;";
    private static final String GREATER_THAN_ENTITY = "&gt;";

    private static final Pattern AMPERSAND_PATTERN = Pattern.compile(AMPERSAND, Pattern.LITERAL);

    // s/</&lt;/g
    private static final Pattern LESS_THAN_PATTERN = Pattern.compile(LESS_THAN, Pattern.LITERAL);

    // s/>/&gt;/g
    private static final Pattern GREATER_THAN_PATTERN = Pattern.compile(GREATER_THAN, Pattern.LITERAL);

    public static String disableHtml(String message) {
        if (IPStringUtil.isEmpty(message)) {
            return message;
        }
        message = AMPERSAND_PATTERN.matcher(message).replaceAll(AMPERSAND_ENTITY);
        message = disableLessThanAndGreaterThan(message);
        return message;
    }

    public static String disableAndTrimHtml(String message) {
        if (IPStringUtil.isEmpty(message)) {
            return message;
        }
        message = disableHtml(message);
        message = IPStringUtil.getTrimmedString(message);
        message = IPStringUtil.removeZeroWidthSpaces(message);
        return message;
    }

    public static String disableLessThanAndGreaterThan(String message) {
        if (IPStringUtil.isEmpty(message)) {
            return message;
        }
        message = LESS_THAN_PATTERN.matcher(message).replaceAll(LESS_THAN_ENTITY);
        message = GREATER_THAN_PATTERN.matcher(message).replaceAll(GREATER_THAN_ENTITY);
        return message;
    }

    public static String escapeAmpersandsForXml(String message) {
        if (IPStringUtil.isEmpty(message)) {
            return message;
        }
        return AMPERSAND_PATTERN.matcher(message).replaceAll(AMPERSAND_ENTITY);
    }

    private static final String QUOTE = "\"";
    private static final String QUOTE_ENTITY = "&quot;";
    private static final Pattern QUOTE_PATTERN = Pattern.compile(QUOTE, Pattern.LITERAL);

    public static String escapeQuotesForXml(String message) {
        if (IPStringUtil.isEmpty(message)) {
            return message;
        }
        return QUOTE_PATTERN.matcher(message).replaceAll(QUOTE_ENTITY);
    }

    private static final String APOSTROPHE = "'";
    private static final String APOSTROPHE_ENTITY = "&apos;";
    private static final Pattern APOSTROPHE_PATTERN = Pattern.compile(APOSTROPHE, Pattern.LITERAL);

    public static String escapeApostrophesForXml(String message) {
        if (IPStringUtil.isEmpty(message)) {
            return message;
        }
        return APOSTROPHE_PATTERN.matcher(message).replaceAll(APOSTROPHE_ENTITY);
    }

    private static final Pattern CDATA_CLOSE_PATTERN = Pattern.compile("]]>", Pattern.LITERAL);
    private static final String CDATA_CLOSE_REPLACEMENT = "]]&gt;";

    /**
     * replace ]]> with ]]&gt;
     * similar to XMLUtil.getCDATACompatibleString, but doesn't do the getValidXmlChars call
     *
     * @param message the text to escape
     * @return the escaped text
     */
    public static String escapeCdataText(String message) {
        if (IPStringUtil.isEmpty(message)) {
            return message;
        }
        return CDATA_CLOSE_PATTERN.matcher(message).replaceAll(CDATA_CLOSE_REPLACEMENT);
    }

    /**
     * sanitize a plain text string.  first, trim() the string.
     * then de-htmlize it.  finally, convert all newline sequences to \n.
     *
     * @param message     the message to sanitize
     * @param disableHtml whether or not to disable html
     * @return the plain text string after sanitization
     */
    public static String sanitizePlainTextString(String message, boolean disableHtml) {
        if (IPStringUtil.isEmpty(message)) {
            return message;
        }
        message = message.trim();
        if (disableHtml) {
            message = HtmlTextMassager.disableHtml(message);
        }
        message = convertNewlineSequencesToCarriageReturns(message);
        return message;
    }

    private static final Pattern AMPERSAND_ENTITY_PATTERN = Pattern.compile(AMPERSAND_ENTITY, Pattern.LITERAL);
    private static final Pattern LESS_THAN_ENTITY_PATTERN = Pattern.compile(LESS_THAN_ENTITY, Pattern.LITERAL);
    private static final Pattern GREATER_THAN_ENTITY_PATTERN = Pattern.compile(GREATER_THAN_ENTITY, Pattern.LITERAL);

    public static String enableDisabledHtml(String message) {
        if (IPStringUtil.isEmpty(message)) {
            return message;
        }
        message = LESS_THAN_ENTITY_PATTERN.matcher(message).replaceAll(LESS_THAN);
        message = GREATER_THAN_ENTITY_PATTERN.matcher(message).replaceAll(GREATER_THAN);
        // bl: do ampersands last so that you don't convert &amp;lt; first into &lt; and then into <.
        // you want &amp;lt; to end up as &lt;
        message = AMPERSAND_ENTITY_PATTERN.matcher(message).replaceAll(AMPERSAND);
        return message;
    }

    // s/(<)(\s)*?script/&lt;$2script/isg
    private static final Pattern SCRIPT_PATTERN = Pattern.compile("(<)(\\s)*?script", Pattern.CASE_INSENSITIVE);
    private static final String SCRIPT_REPLACEMENT = "&lt;$2script";

    /*// s/(<)(\s)*?iframe/&lt;$2iframe/isg
    private static final Pattern IFRAME_PATTERN = Pattern.compile("(<)(\\s)*?iframe", Pattern.CASE_INSENSITIVE);
    private static final String  IFRAME_REPLACEMENT =             "&lt;$2iframe";*/

    /*// s/(<)(\s)*?object/&lt;$2object/isg
    private static final Pattern OBJECT_PATTERN = Pattern.compile("(<)(\\s)*?object", Pattern.CASE_INSENSITIVE);
    private static final String  OBJECT_REPLACEMENT =             "&lt;$2object";

    // s/(<)(\s)*?embed/&lt;$2embed/isg
    private static final Pattern EMBED_PATTERN = Pattern.compile("(<)(\\s)*?embed", Pattern.CASE_INSENSITIVE);
    private static final String  EMBED_REPLACEMENT =             "&lt;$2embed";*/

    public static String killScriptTags(String message) {
        if (IPStringUtil.isEmpty(message)) {
            return message;
        }
        message = SCRIPT_PATTERN.matcher(message).replaceAll(SCRIPT_REPLACEMENT);
        /*message = IFRAME_PATTERN.matcher(message).replaceAll(IFRAME_REPLACEMENT);
        message = OBJECT_PATTERN.matcher(message).replaceAll(OBJECT_REPLACEMENT);
        message = EMBED_PATTERN.matcher(message).replaceAll(EMBED_REPLACEMENT);*/
        return message;
    }

    // s/(<)([^>]*?)(eval\s*?\()/<$2eval_(/isg
    private static final Pattern EVAL_PATTERN = Pattern.compile("(<)([^>]*?)(eval\\s*?\\()", Pattern.CASE_INSENSITIVE);
    private static final String EVAL_REPLACEMENT = "<$2eval_(";

    // s/(<)([^>]*?)(document\s*?\[)/<$2document_(/isg
    private static final Pattern DOCUMENT_PATTERN = Pattern.compile("(<)([^>]*?)(document\\s*?\\[)", Pattern.CASE_INSENSITIVE);
    private static final String DOCUMENT_REPLACEMENT = "<$2document_(";

    // s/(<)([^>]*?)(onerror)/<$2on_error/isg
    private static final Pattern ONERROR_PATTERN = Pattern.compile("(<)([^>]*?)(onerror)", Pattern.CASE_INSENSITIVE);
    private static final String ONERROR_REPLACEMENT = "<$2on_error";

    // s/(<)([^>]*?)(vbscript)/<$2vb_script/isg
    private static final Pattern VBSCRIPT_PATTERN = Pattern.compile("(<)([^>]*?)(vbscript)", Pattern.CASE_INSENSITIVE);
    private static final String VBSCRIPT_REPLACEMENT = "<$2vb_script";

    // s/(<)([^>]*?)(about\s*?:)/<$2about_:/isg
    private static final Pattern ABOUT_PATTERN = Pattern.compile("(<)([^>]*?)(about\\s*?:)", Pattern.CASE_INSENSITIVE);
    private static final String ABOUT_REPLACEMENT = "<$2about_:";

    // s/(<)([^>]*?)(javascript)/<$2java_script/isg
    private static final Pattern JAVASCRIPT_PATTERN = Pattern.compile("(<)([^>]*?)(javascript)", Pattern.CASE_INSENSITIVE);
    private static final String JAVASCRIPT_REPLACEMENT = "<$2java_script";

    // s/(<)([^>]*?)(%28)/<$2(/isg
    private static final Pattern PERCENT_28_PATTERN = Pattern.compile("(<)([^>]*?)(%28)", Pattern.CASE_INSENSITIVE);
    private static final String PERCENT_28_REPLACEMENT = "<$2(";

    // s/(<)([^>]*?)(%63)/<$2c/isg
    private static final Pattern PERCENT_63_PATTERN = Pattern.compile("(<)([^>]*?)(%63)", Pattern.CASE_INSENSITIVE);
    private static final String PERCENT_63_REPLACEMENT = "<$2c";

    // s/(<)([^>]*?)(\()/<$2[/isg
    private static final Pattern OPEN_PAREN_PATTERN = Pattern.compile("(<)([^>]*?)(\\()", Pattern.CASE_INSENSITIVE);
    private static final String OPEN_PAREN_REPLACEMENT = "<$2[";

    // s/(<)([^>]*?)(\.)(\s*)(cookie)/<$2._cookie/isg
    private static final Pattern COOKIE_PATTERN = Pattern.compile("(<)([^>]*?)(\\.)(\\s*)(cookie)", Pattern.CASE_INSENSITIVE);
    private static final String COOKIE_REPLACEMENT = "<$2._cookie";

    public static String killScriptFunctions(String message) {
        if (IPStringUtil.isEmpty(message)) {
            return message;
        }
        /*
        bl: i opted not to bring these over because they seem to arbitrary:
        s/\[!\#\#/[!# #/sg
        s/\<ip:/<ip :/sg
        */
        message = EVAL_PATTERN.matcher(message).replaceAll(EVAL_REPLACEMENT);
        message = DOCUMENT_PATTERN.matcher(message).replaceAll(DOCUMENT_REPLACEMENT);
        message = ONERROR_PATTERN.matcher(message).replaceAll(ONERROR_REPLACEMENT);
        message = VBSCRIPT_PATTERN.matcher(message).replaceAll(VBSCRIPT_REPLACEMENT);
        message = ABOUT_PATTERN.matcher(message).replaceAll(ABOUT_REPLACEMENT);
        message = JAVASCRIPT_PATTERN.matcher(message).replaceAll(JAVASCRIPT_REPLACEMENT);
        // jw: due to image attachments we need to leave URL encoded characters alone!
        //message = PERCENT_28_PATTERN.matcher(message).replaceAll(PERCENT_28_REPLACEMENT);
        //message = PERCENT_63_PATTERN.matcher(message).replaceAll(PERCENT_63_REPLACEMENT);
        message = OPEN_PAREN_PATTERN.matcher(message).replaceAll(OPEN_PAREN_REPLACEMENT);
        // bl: disabling this since it's causing issues with valid HTML like:
        // <a href="http://www.cookie.com" --> <a href="http://www._cookie.com"
        //message = COOKIE_PATTERN.matcher(message).replaceAll(COOKIE_REPLACEMENT);
        return message;
    }

    private static final Collection<String> ATTRIBUTES_SUPPORTED_ON_ALL_HTML_ELEMENTS;

    static {
        Collection<String> attributes = new HashSet<String>();
        // refer: http://www.w3schools.com/tags/ref_standardattributes.asp
        // todo: what about dir, lang, accesskey, tabindex?
        attributes.add("class");
        attributes.add("id");
        attributes.add("style");
        attributes.add("title");
        ATTRIBUTES_SUPPORTED_ON_ALL_HTML_ELEMENTS = Collections.unmodifiableCollection(attributes);
    }

    public static final Map<String, Collection<String>> FROALA_SUPPORTED_HTML_ELEMENTS_TO_SUPPORTED_ATTRIBUTES;
    public static final Map<String, Collection<String>> BODY_SUPPORTED_HTML_ELEMENTS_TO_SUPPORTED_ATTRIBUTES;
    public static final Map<String, Collection<String>> EXTRACT_SUPPORTED_HTML_ELEMENTS_TO_SUPPORTED_ATTRIBUTES;
    public static final Map<String, Collection<NameValuePair<String>>> ELEMENT_NAMES_TO_ATTRIBUTE_NAME_VALUE_PAIRS_TO_ENFORCE;
    public static final Map<String, Collection<NameValuePair<Map<String, String>>>> ELEMENT_NAMES_TO_SUB_ELEMENTS_TO_ENFORCE;
    public static final Map<String, Map<String, Collection<String>>> ELEMENT_NAMES_TO_ATTRIBUTES_WITH_LIMITED_VALUES;

    static {
        Map<String, Collection<String>> map = new HashMap<>();
        // first, get all of the "basic" formatting elements for newlines, links, bold, underline, italics, strikethrough, etc.
        addElementAndAttributesToMap(map, "br", null);
        addElementAndAttributesToMap(map, "p", new String[]{"align"});
        addElementAndAttributesToMap(map, "a", new String[]{"href", "target", "rel", "name"});
        addElementAndAttributesToMap(map, "b", null);
        addElementAndAttributesToMap(map, "strong", null);
        addElementAndAttributesToMap(map, "small", null);
        addElementAndAttributesToMap(map, "var", null);
        addElementAndAttributesToMap(map, "i", null);
        addElementAndAttributesToMap(map, "em", null);
        addElementAndAttributesToMap(map, "strike", null);
        addElementAndAttributesToMap(map, "s", null);
        addElementAndAttributesToMap(map, "del", null);
        addElementAndAttributesToMap(map, "ins", null);
        addElementAndAttributesToMap(map, "u", null);
        addElementAndAttributesToMap(map, "sub", null);
        addElementAndAttributesToMap(map, "sup", null);
        addElementAndAttributesToMap(map, "span", null);
        addElementAndAttributesToMap(map, "div", null);
        addElementAndAttributesToMap(map, "h1", new String[]{"align"});
        addElementAndAttributesToMap(map, "h2", new String[]{"align"});
        addElementAndAttributesToMap(map, "h3", new String[]{"align"});
        addElementAndAttributesToMap(map, "h4", new String[]{"align"});
        addElementAndAttributesToMap(map, "h5", new String[]{"align"});
        addElementAndAttributesToMap(map, "h6", new String[]{"align"});
        addElementAndAttributesToMap(map, "pre", new String[]{"width"});
        addElementAndAttributesToMap(map, "font", new String[]{"size", "color", "face"});

        EXTRACT_SUPPORTED_HTML_ELEMENTS_TO_SUPPORTED_ATTRIBUTES = Collections.unmodifiableMap(new HashMap<String, Collection<String>>(map));

        // extracts don't support things like lists, HRs, images, blockquotes, and tables
        addElementAndAttributesToMap(map, "ul", new String[]{"type"});
        addElementAndAttributesToMap(map, "ol", new String[]{"type"});
        addElementAndAttributesToMap(map, "li", null);
        addElementAndAttributesToMap(map, "hr", new String[]{"align", "size", "width"});
        addElementAndAttributesToMap(map, "img", new String[]{"src", "alt", "align", "border", "height", "hspace", "vspace", "width", "usemap", "ismap"});
        addElementAndAttributesToMap(map, "blockquote", null);
        // refer: http://www.w3schools.com/tags/tag_table.asp
        addElementAndAttributesToMap(map, "table", new String[]{"align", "bgcolor", "border", "cellpadding", "cellspacing", "width"});
        // refer: http://www.w3schools.com/tags/tag_tbody.asp
        addElementAndAttributesToMap(map, "tbody", new String[]{"align", "valign"});
        // refer: http://www.w3schools.com/tags/tag_thead.asp
        addElementAndAttributesToMap(map, "thead", new String[]{"align", "valign"});
        // refer: http://www.w3schools.com/tags/tag_tfoot.asp
        addElementAndAttributesToMap(map, "tfoot", new String[]{"align", "valign"});
        // refer: http://www.w3schools.com/tags/tag_th.asp
        addElementAndAttributesToMap(map, "th", new String[]{"align", "bgcolor", "colspan", "height", "nowrap", "rowspan", "valign", "width"});
        // refer: http://www.w3schools.com/tags/tag_tr.asp
        addElementAndAttributesToMap(map, "tr", new String[]{"align", "bgcolor", "valign"});
        // refer: http://www.w3schools.com/tags/tag_td.asp
        addElementAndAttributesToMap(map, "td", new String[]{"align", "bgcolor", "colspan", "height", "nowrap", "rowspan", "valign", "width"});
        addElementAndAttributesToMap(map, "object", new String[]{"style", "id", "width", "height"});
        addElementAndAttributesToMap(map, "param", new String[]{"name", "value"});
        addElementAndAttributesToMap(map, "embed", new String[]{"style", "id", "src", "menu", "quality", "bgcolor", "flashvars", "width", "height", "name", "align", "allowfullscreen"});
        addElementAndAttributesToMap(map, "iframe", new String[]{"style", "id", "src", "width", "height", "name", "align", "allowfullscreen", "frameborder", "longdesc", "marginheight", "marginwidth", "scrolling", "title"});
        addElementAndAttributesToMap(map, "map", new String[]{"name"});
        addElementAndAttributesToMap(map, "area", new String[]{"shape", "coords", "href", "nohref", "alt", "target"});
        addElementAndAttributesToMap(map, "abbr", null);
        // jw: technically this element has been deprecated by abbr above in HTML5,  including for completeness.
        addElementAndAttributesToMap(map, "acronym", null);
        addElementAndAttributesToMap(map, "address", null);
        addElementAndAttributesToMap(map, "article", null);
        addElementAndAttributesToMap(map, "datalist", null);
        addElementAndAttributesToMap(map, "details", null);
        addElementAndAttributesToMap(map, "summary", null);
        addElementAndAttributesToMap(map, "mark", null);
        addElementAndAttributesToMap(map, "time", new String[]{"datetime"});
        addElementAndAttributesToMap(map, "wbr", null);
        addElementAndAttributesToMap(map, "audio", new String[]{"controls", "loop", "muted", "preload", "src"});
        addElementAndAttributesToMap(map, "video", new String[]{"controls", "loop", "muted", "preload", "src", "height", "poster", "width"});
        addElementAndAttributesToMap(map, "source", new String[]{"media", "src", "type"});
        addElementAndAttributesToMap(map, "figure", null);
        addElementAndAttributesToMap(map, "figcaption", null);
        addElementAndAttributesToMap(map, "caption", new String[]{"align"});
        addElementAndAttributesToMap(map, "cite", null);
        addElementAndAttributesToMap(map, "code", null);
        addElementAndAttributesToMap(map, "samp", null);
        addElementAndAttributesToMap(map, "dfn", null);
        addElementAndAttributesToMap(map, "q", new String[]{"cite"});
        BODY_SUPPORTED_HTML_ELEMENTS_TO_SUPPORTED_ATTRIBUTES = Collections.unmodifiableMap(map);

        // jw: for FROALA, let's just start from scratch and build a small tight set of elements/attributes that we know
        //     they support/use.
        map = new HashMap<>();

        // jw: unlike the above, we want to ensure that we are strict with which attributes we allow, so always specify
        //     a attributes array. HTMLEnforcer currently is setup so that a null attribute array means that all attributes
        //     are supported, and since we want to be very strict about what we allow let's never use that wildcard value.
        addElementAndAttributesToMap(map, "h1", new String[]{});
        addElementAndAttributesToMap(map, "h2", new String[]{});
        addElementAndAttributesToMap(map, "br", new String[]{});
        addElementAndAttributesToMap(map, "p", new String[]{});
        addElementAndAttributesToMap(map, "em", new String[]{});
        addElementAndAttributesToMap(map, "strong", new String[]{});
        addElementAndAttributesToMap(map, "a", new String[]{"href", "target", "rel", "name", "class"});
        addElementAndAttributesToMap(map, "ol", new String[]{});
        addElementAndAttributesToMap(map, "ul", new String[]{});
        addElementAndAttributesToMap(map, "li", new String[]{});
        addElementAndAttributesToMap(map, "hr", new String[]{});
        addElementAndAttributesToMap(map, "blockquote", new String[]{});
        // jw: contenteditable and draggable are necessary for the wrapper placed around embedded content by FROALA. If we strip
        //     these off then the video will not be draggable as part of editing when the author returns later. In testing,
        //     it seems like draggable does nothing to the display, and the element is not treated any differently, so that's good.
        addElementAndAttributesToMap(map, "span", new String[]{"class", "contenteditable", "draggable"});
        addElementAndAttributesToMap(map, "div", new String[]{"class", "data-original-embed", "style"});
        // jw: we will need to change this list once we have support for attachments in the editor. I added "class" since
        //     I expect that FROALA will be using that based on how it uses classes on other elements.
        addElementAndAttributesToMap(map, "img", new String[]{"src", "alt", "align", "border", "height", "hspace", "vspace", "width", "usemap", "ismap", "class"});
        // jw: FROALA uses embedly to get embed code from URLs, so it's going to be difficult to know exactly what elements and attributes
        //     they may need for their 50+ video providers that they support:
        //     https://embed.ly/providers
        //     Since it is impossible to know all of the combinations, and all the attributes that are necessary, or guarantee
        //     that they will not change, let's just add all the common elements with all the safe attributes and call it good.
        addElementAndAttributesToMap(map, "object", new String[]{"style", "id", "width", "height", "class"});
        addElementAndAttributesToMap(map, "param", new String[]{"name", "value"});
        addElementAndAttributesToMap(map, "embed", new String[]{"style", "id", "src", "menu", "quality", "bgcolor", "flashvars", "width", "height", "name", "align", "allowfullscreen", "class"});
        addElementAndAttributesToMap(map, "iframe", new String[]{"style", "id", "src", "width", "height", "name", "align", "allowfullscreen", "frameborder", "longdesc", "marginheight", "marginwidth", "scrolling", "title", "class"});
        addElementAndAttributesToMap(map, "video", new String[]{"controls", "loop", "muted", "preload", "src", "height", "poster", "width", "class"});
        addElementAndAttributesToMap(map, "source", new String[]{"media", "src", "type"});

        FROALA_SUPPORTED_HTML_ELEMENTS_TO_SUPPORTED_ATTRIBUTES = Collections.unmodifiableMap(map);

        Map<String, Collection<NameValuePair<String>>> elementsWithAttributesToEnforce = new HashMap<String, Collection<NameValuePair<String>>>();

        Collection<NameValuePair<String>> embedAttributesToEnforce = new HashSet<NameValuePair<String>>();
        embedAttributesToEnforce.add(new NameValuePair<String>("allowScriptAccess", "never"));
        embedAttributesToEnforce.add(new NameValuePair<String>("type", "application/x-shockwave-flash"));
        embedAttributesToEnforce.add(new NameValuePair<String>("pluginspage", "http://www.macromedia.com/go/getflashplayer"));
        embedAttributesToEnforce.add(new NameValuePair<String>("wmode", "transparent"));
        elementsWithAttributesToEnforce.put("embed", Collections.unmodifiableCollection(embedAttributesToEnforce));

        Collection<NameValuePair<String>> objectAttributesToEnforce = new HashSet<NameValuePair<String>>();
        objectAttributesToEnforce.add(new NameValuePair<String>("classid", "clsid:d27cdb6e-ae6d-11cf-96b8-444553540000"));
        objectAttributesToEnforce.add(new NameValuePair<String>("codebase", "http://fpdownload.macromedia.com/pub/shockwave/cabs/flash/swflash.cab#version=8,0,0,0"));
        elementsWithAttributesToEnforce.put("object", Collections.unmodifiableCollection(objectAttributesToEnforce));

        ELEMENT_NAMES_TO_ATTRIBUTE_NAME_VALUE_PAIRS_TO_ENFORCE = Collections.unmodifiableMap(elementsWithAttributesToEnforce);

        Map<String, Collection<NameValuePair<Map<String, String>>>> elementsWithSubElementsToEnforce = new HashMap<String, Collection<NameValuePair<Map<String, String>>>>();
        Collection<NameValuePair<Map<String, String>>> objectSubElementsToEnforce = new HashSet<NameValuePair<Map<String, String>>>();
        // force allowScriptAccess to never
        {
            Map<String, String> objectSubParamAttributes = new HashMap<String, String>();
            objectSubParamAttributes.put("name", "allowScriptAccess");
            objectSubParamAttributes.put("value", "never");
            objectSubElementsToEnforce.add(new NameValuePair<Map<String, String>>("param", Collections.unmodifiableMap(objectSubParamAttributes)));
        }
        {
            Map<String, String> objectSubParamAttributes = new HashMap<String, String>();
            objectSubParamAttributes.put("name", "wmode");
            objectSubParamAttributes.put("value", "transparent");
            objectSubElementsToEnforce.add(new NameValuePair<Map<String, String>>("param", Collections.unmodifiableMap(objectSubParamAttributes)));
        }
        elementsWithSubElementsToEnforce.put("object", Collections.unmodifiableCollection(objectSubElementsToEnforce));
        ELEMENT_NAMES_TO_SUB_ELEMENTS_TO_ENFORCE = Collections.unmodifiableMap(elementsWithSubElementsToEnforce);

        Map<String, Map<String, Collection<String>>> elementNamesToAttributesWithLimitedValues = new HashMap<String, Map<String, Collection<String>>>();

        addElementAttributeWithLimitedValues(
                elementNamesToAttributesWithLimitedValues
                , "param"
                , "name"
                , Arrays.asList("src", "width", "height", "movie", "menu", "quality", "bgcolor", "flashvars", "allowfullscreen")
        );
        ELEMENT_NAMES_TO_ATTRIBUTES_WITH_LIMITED_VALUES = Collections.unmodifiableMap(elementNamesToAttributesWithLimitedValues);
    }

    private static void addElementAttributeWithLimitedValues(Map<String, Map<String, Collection<String>>> elementNamesToAttributesWithLimitedValues, String elementName, String attribute, Collection<String> limitedValues) {
        Map<String, Collection<String>> paramElementAttributeLimitedValues = new HashMap<String, Collection<String>>();
        Collection<String> paramElementNameAttributeLimitedValues = new HashSet<String>();
        paramElementNameAttributeLimitedValues.addAll(limitedValues);
        paramElementAttributeLimitedValues.put(attribute, Collections.unmodifiableCollection(paramElementNameAttributeLimitedValues));
        elementNamesToAttributesWithLimitedValues.put(elementName, Collections.unmodifiableMap(paramElementAttributeLimitedValues));
    }

    private static void addElementAndAttributesToMap(Map<String, Collection<String>> map, String elementName, String[] attributeNames) {
        Collection<String> attributes = new HashSet<String>(ATTRIBUTES_SUPPORTED_ON_ALL_HTML_ELEMENTS);
        if (attributeNames != null) {
            attributes.addAll(Arrays.asList(attributeNames));
        }
        map.put(elementName, Collections.unmodifiableCollection(attributes));
    }

    public static String escapeUnsupportedHtml(String message, boolean isBody, HTMLEnforcer.Options options) {
        return escapeUnsupportedHtml(
                message,
                isBody ? BODY_SUPPORTED_HTML_ELEMENTS_TO_SUPPORTED_ATTRIBUTES : EXTRACT_SUPPORTED_HTML_ELEMENTS_TO_SUPPORTED_ATTRIBUTES,
                options
        );
    }

    public static String escapeUnsupportedHtml(String message, Map<String, Collection<String>> supportedElementsToAttributes, HTMLEnforcer.Options options) {
        if (IPStringUtil.isEmpty(message)) {
            return message;
        }

        return HTMLEnforcer.enforceHtmlElementsAndAttributes(message, options, supportedElementsToAttributes, ELEMENT_NAMES_TO_ATTRIBUTE_NAME_VALUE_PAIRS_TO_ENFORCE, ELEMENT_NAMES_TO_SUB_ELEMENTS_TO_ENFORCE, ELEMENT_NAMES_TO_ATTRIBUTES_WITH_LIMITED_VALUES, HTMLParser.FragmentType.BODY);
    }

    public static final Map<String, Collection<String>> HEAD_SUPPORTED_HTML_ELEMENTS_TO_SUPPORTED_ATTRIBUTES;
    public static final Map<String, Collection<NameValuePair<String>>> HEAD_ELEMENT_NAMES_TO_ATTRIBUTE_NAME_VALUE_PAIRS_TO_ENFORCE;
    public static final Map<String, Collection<NameValuePair<Map<String, String>>>> HEAD_ELEMENT_NAMES_TO_SUB_ELEMENTS_TO_ENFORCE;
    public static final Map<String, Map<String, Collection<String>>> HEAD_ELEMENT_NAMES_TO_ATTRIBUTES_WITH_LIMITED_VALUES;

    static {
        Map<String, Collection<String>> map = new HashMap<String, Collection<String>>();
        // jw: Note: this is the only element for the head that I using our standard method to setup, this is because
        //     none of the other elements have the default attributes (style, id etc)
        addElementAndAttributesToMap(map, "link", new String[]{"charset", "href", "hreflang", "media", "rel", "rev", "target", "type"});

        map.put("style", newHashSet("media"));
        map.put("meta", newHashSet(Arrays.asList("name", "content")));

        HEAD_SUPPORTED_HTML_ELEMENTS_TO_SUPPORTED_ATTRIBUTES = Collections.unmodifiableMap(new HashMap<String, Collection<String>>(map));

        Map<String, Collection<NameValuePair<String>>> elementsWithAttributesToEnforce = new HashMap<String, Collection<NameValuePair<String>>>();

        Collection<NameValuePair<String>> linkAttributesToEnforce = new HashSet<NameValuePair<String>>();

        // jw: Forcing this to type="text/css" since its a required attribute with only this value
        //     http://www.w3schools.com/tags/tag_style.asp
        Collection<NameValuePair<String>> styleAttributesToEnforce = new HashSet<NameValuePair<String>>();
        styleAttributesToEnforce.add(new NameValuePair<String>("type", "text/css"));
        elementsWithAttributesToEnforce.put("style", Collections.unmodifiableCollection(styleAttributesToEnforce));

        HEAD_ELEMENT_NAMES_TO_ATTRIBUTE_NAME_VALUE_PAIRS_TO_ENFORCE = Collections.unmodifiableMap(elementsWithAttributesToEnforce);

        Map<String, Collection<NameValuePair<Map<String, String>>>> elementsWithSubElementsToEnforce = new HashMap<String, Collection<NameValuePair<Map<String, String>>>>();
        HEAD_ELEMENT_NAMES_TO_SUB_ELEMENTS_TO_ENFORCE = Collections.unmodifiableMap(elementsWithSubElementsToEnforce);

        Map<String, Map<String, Collection<String>>> elementNamesToAttributesWithLimitedValues = new HashMap<String, Map<String, Collection<String>>>();
        Map<String, Collection<String>> paramElementAttributeLimitedValues = new HashMap<String, Collection<String>>();
        Collection<String> paramElementNameAttributeLimitedValues = new HashSet<String>();

        // jw: Decided to limit the meta tags to description and keywords, since none of the other ones should be settable
        //     by a admin who is not paying us for access to them  :D
        //     http://www.w3schools.com/tags/tag_meta.asp
        paramElementNameAttributeLimitedValues.addAll(Arrays.asList("description", "keywords"));
        paramElementAttributeLimitedValues.put("name", Collections.unmodifiableCollection(paramElementNameAttributeLimitedValues));
        elementNamesToAttributesWithLimitedValues.put("meta", Collections.unmodifiableMap(paramElementAttributeLimitedValues));

        HEAD_ELEMENT_NAMES_TO_ATTRIBUTES_WITH_LIMITED_VALUES = Collections.unmodifiableMap(elementNamesToAttributesWithLimitedValues);
    }

    private static final Pattern ENTITY_PATTERN = Pattern.compile("&\\#?\\w+;");

    public static String stripEntities(String text) {
        return ENTITY_PATTERN.matcher(text).replaceAll("");
        /*StringBuilder sb = new StringBuilder(text.length());
        int start;
        int end = 0;
        while ((start = text.indexOf('&', end)) != -1) {
            sb.append(text.substring(end, start));
            end = text.indexOf(';', start);
            if (end == -1)
                break;
            else
                end++;
        }
        sb.append(text.substring(end));
        return sb.toString();*/
    }

    public static String getBodyAsExtract(String body, int maxLength, Map<String, Graemlin> keystrokeToGraemlins) {
        body = HTMLStripper.stripBlockQuoteElement(body);
        // bl: start by converting &nbsp; and cr/lf to spaces. want the extract as a single line.
        body = HtmlTextMassager.convertNbspToSpace(body);
        body = HtmlTextMassager.convertCrAndLfToSpaces(body);
        // bl: now, strip all HTML from the body. leave entities in tact, however. we'll deal with those below.
        body = HTMLStripper.stripHtmlFragment(body, false);
        // convert any &amp;, &lt;, &gt; to their proper counterparts so we can strip out all other entities
        body = HtmlTextMassager.enableDisabledHtml(body);
        // now strip out any remaining entities
        body = HtmlTextMassager.stripEntities(body);
        // now that we have stripped out entities, we can convert <, >, & back to their entities. those should
        // be the only remaining entities in the body at that point.
        body = HtmlTextMassager.disableHtml(body);
        // remove any consecutive spaces that may have happened due to above massaging
        body = HtmlTextMassager.convertConsecutiveSpacesToSingleSpace(body);

        // jw: let's process any emoji graemlins so that the keystrokes will be converted into the emoji character
        if (keystrokeToGraemlins != null) {
            body = GraemlinMassager.doGraemlinsToHtml(body, keystrokeToGraemlins, GraemlinType.EMOJI);
        }

        // trim spaces from beginning/end of body.
        body = body.trim();
        // finally, ensure that the string is truncated to the proper max length
        return IPStringUtil.getStringTruncatedToEndOfWord(body, maxLength);
    }

    private static final Pattern CONDITIONAL_COMMENT_PATTERN = Pattern.compile("<!--\\s*\\[if (.*)\\]>(.*)\\[endif\\]\\s*-->", Pattern.MULTILINE);

    public static String stripConditionalComments(String text) {
        return CONDITIONAL_COMMENT_PATTERN.matcher(text).replaceAll("");
    }

    private static final Pattern STACK_TRACE_ELEMENT_PATTERN = Pattern.compile("\\tat \\S+");

    public static String stackTraceAsHtml(Throwable e) {
        String ret = disableHtml(Debug.stackTraceFromException(e));
        // bl: wrap each stack trace line in a nobr tag to prevent needless wrapping after the "at" which makes
        // the stack trace a lot harder to read.
        StringBuffer sb = new StringBuffer();
        Matcher matcher = STACK_TRACE_ELEMENT_PATTERN.matcher(ret);
        while (matcher.find()) {
            String stackTraceLine = matcher.group();
            // use one space followed by &nbsp;
            StringBuilder noBrString = new StringBuilder();
            noBrString.append("<nobr>");
            noBrString.append(Matcher.quoteReplacement(stackTraceLine));
            noBrString.append("</nobr>");
            matcher.appendReplacement(sb, noBrString.toString());
        }
        ret = sb.toString();
        return convertCrAndLfToHtml(ret);
    }

    public static String escapePotentiallyEscapedValue(String value) {
        if (isEmpty(value)) {
            return value;
        }

        if (isEqual(value, StringEscapeUtils.UNESCAPE_HTML4.translate(value))) {
            return disableHtml(value);
        }

        return disableLessThanAndGreaterThan(value);
    }
}
