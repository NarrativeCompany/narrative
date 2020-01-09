package org.narrative.common.util;

import org.narrative.common.persistence.OID;
import org.narrative.common.persistence.ResultSetInfo;
import org.narrative.common.util.posting.HtmlTextMassager;
import org.w3c.dom.Attr;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.AttributeList;
import org.xml.sax.Attributes;
import org.xml.sax.HandlerBase;
import org.xml.sax.InputSource;
import org.xml.sax.Parser;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.LineNumberReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Field;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.regex.Pattern;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Convenience class for a number of static utility methods.
 */
public class XMLUtil {

    private static final NarrativeLogger logger = new NarrativeLogger(XMLUtil.class);

    static boolean[] xmlElementSpecialChars;         // lookup table for special characters
    static boolean[] xmlAttributeSpecialChars;         // lookup table for special characters

    public static final String CDATA_OPEN = "<![CDATA[";
    public static final String CDATA_CLOSE = "]]>";

    // create look-up table for ASCII characters that need special treatment

    static {
        xmlElementSpecialChars = new boolean[128];
        xmlAttributeSpecialChars = new boolean[128];
        for (int i = 0; i < 128; i++) {
            xmlAttributeSpecialChars[i] = xmlElementSpecialChars[i] = false;
        }
        // attribute special chars (only): <&"
        xmlAttributeSpecialChars['<'] = xmlElementSpecialChars['<'] = true;
        xmlElementSpecialChars['>'] = true;
        xmlAttributeSpecialChars['&'] = xmlElementSpecialChars['&'] = true;
        xmlElementSpecialChars['\''] = true;
        xmlAttributeSpecialChars['\"'] = xmlElementSpecialChars['\"'] = true;
    }

    /**
     * Format a start tag given the element name and the attribute list
     *
     * @param name The element name
     * @param atts The attribute list
     * @return a String containing a valid XML start tag
     */

    public static final String getTag(String name, Attributes atts) {
        StringBuilder sb = new StringBuilder();
        sb.append("<");
        sb.append(name);
        int attNr = atts == null ? 0 : atts.getLength();
        if (attNr > 0) {

            for (int i = 0; i < attNr; i++) {
                String aname = atts.getLocalName(i);
                sb.append(" ");
                sb.append(aname);
                sb.append("=\"");
                sb.append(getEscapedXML(atts.getValue(i)));
                sb.append("\"");
            }
        }
        sb.append(">");
        return sb.toString();
    }

    /**
     * for name=foo and value="&"val" return name="&amp;&quot;val"
     */
    public static <T extends Appendable> void addNameEqualsValueAttribute(T buf, String name, String value) {
        addNameEqualsValueAttribute(buf, name, value, false);
    }

    public static <T extends Appendable> void addNameEqualsValueAttribute(T buf, String name, String value, boolean isForHtml) {
        try {
            buf.append(' ');
            buf.append(name);
            // bl: if it's for HTML with a null value, then there doesn't need to be an attribute value included.
            if (value != null || !isForHtml) {
                buf.append("=\"");
                buf.append(getEscapedXMLForAttribute(value, isForHtml));
                buf.append("\"");
            }
        } catch (IOException ioe) {
            throw UnexpectedError.getRuntimeException("Failed adding XML attribute!", ioe, true);
        }
    }

    /*
    removed since not used (may work, if you use it, test it first)
    *
     * the spec says:
     * [4]  NameChar ::=  Letter | Digit | '.' | '-' | '_' | ':' | CombiningChar | Extender
     * [5]  Name ::=  (Letter | '_' | ':') (NameChar)*
     * [6]  Names ::=  Name (S Name)*
     * [7]  Nmtoken ::=  (NameChar)+
     * [8]  Nmtokens ::=  Nmtoken (S Nmtoken)*
     * we're returning the (starts with a letter), alpha numeric
     *
    static public String getXMLName(String name) {
        if (IPStringUtil.isEmpty(name))
            return name;
        char ch[] = name.toCharArray();
        for (int i=0; i < ch.length; i++) {
            if (
               (i>0 && ch[i]>='0' && ch[i]<='9')
               || (
                  (ch[i]>='a' && ch[i] <= 'z')
                  ||  (ch[i]>='A' && ch[i] <= 'Z'))) {
                ;
            } else {
                if (i==0)
                    ch[i]='a';
                else ch[i]='_';
            }
        }
        return new String(ch);
    }
    */

    /**
     * Escape special characters for display.
     *
     * @param ch     The character array containing the string
     * @param start  The start position of the input string within the character array
     * @param length The length of the input string within the character array
     * @return The XML/HTML representation of the string<br>
     * This static method converts a Unicode string to a string containing
     * only ASCII characters, in which non-ASCII characters are represented
     * by the usual XML/HTML escape conventions (for example, "&lt;" becomes "&amp;lt;").
     * Note: if the input consists solely of ASCII or Latin-1 characters,
     * the output will be equally valid in XML and HTML. Otherwise it will be valid
     * only in XML.
     */

    public static final String getASCIIdAndEscapedXML(String in, char ch[], int start, int length) {
        return getValidXMLChars0(in, ch, start, length, true, true, false);
    }

    /**
     * escapes xml for &lt;, &gt; ' " and makes sure characters are valid xml:
     * Char ::=  #x9 | #xA | #xD | [#x20-#xD7FF] | [#xE000-#xFFFD] | [#x10000-#x10FFFF]
     */
    public static final String getEscapedXML(String in, char ch[], int start, int length) {
        return getValidXMLChars0(in, ch, start, length, false, true, false);
    }

    public static final String getEscapedXMLForAttribute(String in) {
        return getEscapedXMLForAttribute(in, false);
    }

    public static final String getEscapedXMLForAttribute(String in, boolean isForHtml) {
        char ch[] = in == null ? new char[0] : in.toCharArray();
        return getValidXMLChars0(in, ch, 0, ch.length, false, true, true, isForHtml);
    }

    /**
     * @param isForAttribute: the following (only) need escaping for attribute values: ^<&"
     */
    public static final String getValidXMLChars0(String in, char ch[], int start, int length, boolean escapeAscii, boolean escapeSpecialXML, boolean isForAttributeValue) {
        return getValidXMLChars0(in, ch, start, length, escapeAscii, escapeSpecialXML, isForAttributeValue, false);
    }

    public static final String getValidXMLChars0(String in, char ch[], int start, int length, boolean escapeAscii, boolean escapeSpecialXML, boolean isForAttributeValue, boolean isForHtml) {
        // Use a character array for performance reasons;
        // allocate size on the basis that it might be all non-ASCII

        // allow for worst case
        //pb char[] out = new char[length *8];
        // pb make it a little less memory greedy
        char[] out = new char[length + Math.max(10, Math.min(1024, (int) (length * .1)))];
        int o = 0;
        boolean hasChanged = false;
        int firstMaxValidChar = escapeAscii ? 0x7f : 0xD7FF;
        for (int i = start; i < start + length; i++) {
            // need to resize output?
            if (o + 10 > out.length) {
                char tmp[] = new char[out.length * 2];
                System.arraycopy(out, 0, tmp, 0, out.length);
                out = tmp;
            }
            char current = ch[i];
            //  Char ::=  #x9 | #xA | #xD | [#x20-#xD7FF] | [#xE000-#xFFFD] | [#x10000-#x10FFFF]

            if (current < 0x20) {
                switch (current) {
                    case 0x9:
                    case 0xa:
                    case 0xd:
                        out[o] = current;
                        o++;
                        break;
                    default:
                        // its invalid, ignore it
                        hasChanged = true;
                        break;
                }
            } else if (escapeSpecialXML && current < 0x7f && !isForAttributeValue && xmlElementSpecialChars[current]) {
                hasChanged = true;
                switch (current) {
                    case '<': {
                        ("&lt;").getChars(0, 4, out, o);
                        o += 4;
                        hasChanged = true;
                        break;
                    }
                    case '>': {
                        ("&gt;").getChars(0, 4, out, o);
                        o += 4;
                        hasChanged = true;
                        break;
                    }
                    case '&': {
                        ("&amp;").getChars(0, 5, out, o);
                        o += 5;
                        hasChanged = true;
                        break;
                    }
                    case '\"': {
                        //("&#34;").getChars(0,5,out,o); o+=5;hasChanged = true;break;
                        // bl: &quot; is the standard XML entity.  let's go with it.
                        ("&quot;").getChars(0, 6, out, o);
                        o += 6;
                        hasChanged = true;
                        break;
                    }
                    case '\'': {
                        if (isForHtml) {
                            // bl: for HTML, &apos; isn't supported fully.  so, use &#39; instead.
                            // refer: http://www.w3.org/TR/xhtml1/#C_16
                            ("&#39;").getChars(0, 5, out, o);
                            o += 5;
                            hasChanged = true;
                            break;
                        } else {
                            // bl: &apos; is the standard XML entity.  let's go with it.
                            ("&apos;").getChars(0, 6, out, o);
                            o += 6;
                            hasChanged = true;
                            break;
                        }
                    }
                }
            } else if (escapeSpecialXML && current < 0x7f && isForAttributeValue && xmlAttributeSpecialChars[current]) {
                hasChanged = true;
                switch (current) {
                    case '<': {
                        ("&lt;").getChars(0, 4, out, o);
                        o += 4;
                        hasChanged = true;
                        break;
                    }
                    case '&': {
                        ("&amp;").getChars(0, 5, out, o);
                        o += 5;
                        hasChanged = true;
                        break;
                    }
                    case '\"': {
                        //("&#34;").getChars(0,5,out,o); o+=5;hasChanged = true;break;
                        // bl: &quot; is the standard XML entity.  let's go with it.
                        // using the numerical entity caused some stuff to break for custom graemlin attributes
                        // when some fields such as the keystroke or the name had " in them.
                        ("&quot;").getChars(0, 6, out, o);
                        o += 6;
                        hasChanged = true;
                        break;
                    }
                }
                // bl: let's also allow surrogate pairs through so that emojis will not be stripped from the output here.
            } else if (current < 0xD7FF || Character.isHighSurrogate(current) || Character.isLowSurrogate(current)) {
                out[o] = current;
                o++;
            } else {
                // [#xE000-#xFFFD] | [#x10000-#x10FFFF]
                boolean isValid = !escapeAscii && ((current >= 0xE000 && current <= 0xFFFD) || (current >= 0x10000 && current <= 0x10FFFF));
                if (isValid) {
                    out[o] = current;
                    o++;
                } else {
                    // invalid.  was getting xml errors on &#55589; == &#xD925;
                    //String dec = "&#" + Integer.toString((int)current) + ';';
                    //dec.getChars(0, dec.length(), out, o);
                    //o+=dec.length();
                    hasChanged = true;
                }
            }
        }
        // save the creation of a single string (x thousands of invocations)
        if (hasChanged) {
            return new String(out, 0, o);
        }
        return in;
    }

    /**
     * Escape special characters in a String value.
     *
     * @param in The input string
     * @return The XML representation of the string<br>
     * This static method converts a Unicode string to a string containing
     * only ASCII characters, in which non-ASCII characters are represented
     * by the usual XML/HTML escape conventions (for example, "&lt;" becomes
     * "&amp;lt;").<br>
     * Note: if the input consists solely of ASCII or Latin-1 characters,
     * the output will be equally valid in XML and HTML. Otherwise it will be valid
     * only in XML.
     */

    public static final String getASCIIdAndEscapedXML(String in) {
        if (in == null || in.length() == 0) {
            return in;
        }
        return getASCIIdAndEscapedXML(in, in.toCharArray(), 0, in.length());
    }

    public static final String getEscapedXML(String in) {
        if (in == null || in.length() == 0) {
            return in;
        }
        return getEscapedXML(in, in.toCharArray(), 0, in.length());
    }

    /**
     * Construct an alphabetic key from an positive integer; the key collates in the same sequence
     * as the integer
     *
     * @param value The positive integer key value (negative values are treated as zero).
     */

    public static final String alphaKey(int value) {
        if (value < 1) {
            return "a";
        }
        if (value < 10) {
            return "b" + value;
        }
        if (value < 100) {
            return "c" + value;
        }
        if (value < 1000) {
            return "d" + value;
        }
        if (value < 10000) {
            return "e" + value;
        }
        if (value < 100000) {
            return "f" + value;
        }
        if (value < 1000000) {
            return "g" + value;
        }
        if (value < 10000000) {
            return "h" + value;
        }
        if (value < 100000000) {
            return "i" + value;
        }
        if (value < 1000000000) {
            return "j" + value;
        }
        return "k" + value;
    }

    /**
     * set the xml version and prepend a stylesheet ref
     */
    static public StringBuffer getXMLPrefix(String styleSheet) {
        return getXMLPrefix(styleSheet, null);
    }

    static public StringBuffer getXMLPrefix(String styleSheet, String encoding) {
        StringBuffer buf = new StringBuffer();
        buf.append("<?xml version =\"1.0\" " + (encoding == null ? "" : " encoding=\"" + encoding + "\"") + " ?>\n");
        if (styleSheet != null) {
            buf.append("<?xml:stylesheet href=\"" + styleSheet + "\" ?>\n");
        }
        return buf;
    }

    public static final <T extends Appendable> T openTag(T buf, String tag) {
        try {
            return (T) buf.append('<').append(tag + '>' + '\n');
        } catch (IOException e) {
            throw UnexpectedError.getRuntimeException("Unable to append", e, true);
        }
    }

    public static final <T extends Appendable> T openTagWithAttribute(T buf, String tag, String name, String value) {
        try {
            buf.append('<').append(tag);
            addNameEqualsValueAttribute(buf, name, value);
            buf.append(">\n");

            return (T) buf;
        } catch (IOException e) {
            throw UnexpectedError.getRuntimeException("Unable to append", e, true);
        }
    }

    public static final <T extends Appendable> T openTagWithAttributes(T buf, String tag, Map<String, String> attributes) {
        try {
            buf.append('<').append(tag);
            for (Map.Entry<String, String> entry : attributes.entrySet()) {
                addNameEqualsValueAttribute(buf, entry.getKey(), entry.getValue());
            }
            buf.append(">\n");

            return (T) buf;
        } catch (IOException e) {
            throw UnexpectedError.getRuntimeException("Unable to append", e, true);
        }
    }

    public static final <T extends Appendable> T addField(T buf, String tag, int value) {
        try {
            return (T) buf.append('<').append(tag + '>' + Integer.toString(value) + '<' + '/' + tag + '>' + '\n');
        } catch (IOException e) {
            throw UnexpectedError.getRuntimeException("Unable to append", e, true);
        }

    }

    public static final <T extends Appendable> T addField(T buf, String tag, Boolean value) {
        return (T) addField(buf, tag, value == null ? null : value.booleanValue() ? "Y" : "N");
    }

    public static final <T extends Appendable> T addField(T buf, String tag, boolean value) {
        return (T) addField(buf, tag, value ? "Y" : "N");
    }

    public static final <T extends Appendable> T addField(T buf, String tag, float value) {
        try {
            return (T) buf.append('<').append(tag + '>' + Float.toString(value) + '<' + '/' + tag + '>' + '\n');
        } catch (IOException e) {
            throw UnexpectedError.getRuntimeException("Unable to append", e, true);
        }
    }

    public static final <T extends Appendable> T addField(T buf, String tag, long value) {
        try {
            return (T) buf.append('<').append(tag + '>' + Long.toString(value) + '<' + '/' + tag + '>' + '\n');
        } catch (IOException e) {
            throw UnexpectedError.getRuntimeException("Unable to append", e, true);
        }
    }

    public static final <T extends Appendable> T addField(T buf, String tag, String value) {
        try {
            if (IPStringUtil.isEmpty(value)) {
                return (T) buf.append('<').append(tag).append('/').append('>').append('\n');
            }
            return (T) buf.append('<').append(tag).append('>').append(getEscapedXML(value)).append('<').append('/').append(tag).append('>').append('\n');
        } catch (IOException e) {
            throw UnexpectedError.getRuntimeException("Unable to append", e, true);
        }
    }

    public static final <T extends Appendable> T addField(T buf, String tag, String value, String attributeName, String attributeValue) {
        try {
            buf.append('<').append(tag);
            addNameEqualsValueAttribute(buf, attributeName, attributeValue);
            if (IPStringUtil.isEmpty(value)) {
                return (T) buf.append('/').append('>').append('\n');
            }
            return (T) buf.append('>').append(getEscapedXML(value)).append('<').append('/').append(tag).append('>').append('\n');
        } catch (IOException e) {
            throw UnexpectedError.getRuntimeException("Unable to append", e, true);
        }
    }

    public static final <T extends Appendable> T addField(T buf, String tag, String value, Map<String, String> attributes) {
        try {
            buf.append('<').append(tag);
            if (!isEmptyOrNull(attributes)) {
                for (Map.Entry<String, String> attribute : attributes.entrySet()) {
                    addNameEqualsValueAttribute(buf, attribute.getKey(), attribute.getValue());
                }
            }
            if (IPStringUtil.isEmpty(value)) {
                return (T) buf.append('/').append('>').append('\n');
            }
            return (T) buf.append('>').append(getEscapedXML(value)).append('<').append('/').append(tag).append('>').append('\n');
        } catch (IOException e) {
            throw UnexpectedError.getRuntimeException("Unable to append", e, true);
        }
    }

    public static final <T extends Appendable> T addField(T buf, String tag, OID oid) {
        return (T) addField(buf, tag, oid == null ? "" : oid.toString());
    }

    public static final <T extends Appendable> T addCDATAField(T buf, String tag, String value) {
        try {
            if (IPStringUtil.isEmpty(value)) {
                return (T) buf.append('<').append(tag).append('/').append('>').append('\n');
            }
            return (T) buf.append("<").append(tag).append("><![CDATA[").append(getCDATACompatibleString(value)).append("]]></").append(tag).append('>').append('\n');
        } catch (IOException e) {
            throw UnexpectedError.getRuntimeException("Unable to append", e, true);
        }
    }

    private static final Pattern CDATA_CLOSING_ESCAPE_PATTERN = Pattern.compile("\\]\\]>");

    /**
     * makes sure the string has no invalid XML chars
     */
    public static String getCDATACompatibleString(String data) {
        if (IPStringUtil.isEmpty(data)) {
            return data;
        }
        int len = data.length();
        data = getValidXMLChars0(data, data.toCharArray(), 0, len, false, false, false);
        if (len > 3 && data.indexOf("]]>") > 0) {
            data = CDATA_CLOSING_ESCAPE_PATTERN.matcher(data).replaceAll("]]&gt;");//TextReplacer.getInstance().replace("s/\\]\\]\\>/]]&gt;/sg", data);
        }
        return data;
        /*
        int i =0;
        do {
        int pos = data.indexOf("]]>", i);
        if(pos<0)
        break;
        //wrong: needs to be from last pos (not 0) to current pos
        data = (pos == 0
        ? ""
        : data.substring(0, pos)
        )
        +"]]&gt;"
        + data.substring(pos + 3,
        */
    }

    public static final <T extends Appendable> T closeTag(T buf, String tag) {
        try {
            return (T) buf.append("</" + tag + '>' + '\n');
        } catch (IOException e) {
            throw UnexpectedError.getRuntimeException("Unable to append", e, true);
        }
    }

    public static final void openTag(Writer buf, String tag) throws IOException {
        buf.write('<' + tag + '>' + '\n');
    }

    public static final void addField(Writer buf, String tag, int value) throws IOException {
        buf.write("<" + tag + '>' + Integer.toString(value) + '<' + '/' + tag + '>' + '\n');
    }

    public static final void addField(Writer buf, String tag, String value) throws IOException {
        if (IPStringUtil.isEmpty(value)) {
            buf.write("<" + tag + '/' + '>' + '\n');
        } else {
            buf.write("<" + tag + '>' + getEscapedXML(value) + '<' + '/' + tag + '>' + '\n');
        }
    }

    public static final void addField(Writer buf, String tag, float value) throws IOException {
        buf.write("<" + tag + '>' + Float.toString(value) + '<' + '/' + tag + '>' + '\n');
    }

    public static final void addCDATAField(Writer buf, String tag, String value) throws IOException {
        if (IPStringUtil.isEmpty(value)) {
            buf.write("<" + tag + '/' + '>' + '\n');
        }
        buf.write("<" + tag + "><![CDATA[" + getCDATACompatibleString(value) + "]]></" + tag + '>' + '\n');
    }

    public static final void closeTag(Writer buf, String tag) throws IOException {
        buf.write("</" + tag + '>' + '\n');
    }

    private static DocumentBuilderFactory s_documentBuilderFactory = null;

    /**
     * create an app wide doc builder factory.  save a bit of time doing DocumentBuilderFactory.newInstance
     * per: http://jcp.org/aboutJava/communityprocess/review/jsr063/jaxp-pd2.pdf
     * It is expected that the newDocumentBuilder
     * method of a DocumentBuilderFactory is thread safe.
     */
    public static DocumentBuilderFactory getDocumentBuilderFactory() {
        if (s_documentBuilderFactory == null) {
            DocumentBuilderFactory b = DocumentBuilderFactory.newInstance();
            b.setExpandEntityReferences(false);
            s_documentBuilderFactory = b;
        }
        return s_documentBuilderFactory;
    }

    static SAXParserFactory s_saxParserFactory = SAXParserFactory.newInstance();

    public static Parser createParser() {
        try {
            //if(logger.isInfoEnabled()) logger.info( "xml: trying the saxparser factory");
            //pb: removing this cause it was showing up as a 'hot spot' during
            // app profiling.
            // SAXParserFactory s_spf = SAXParserFactory.newInstance();

            // per: http://jcp.org/aboutJava/communityprocess/review/jsr063/jaxp-pd2.pdf
            // It is expected that the newSAXParser method of a SAXParserFactory implementation, the newDocumentBuilder
            // method of a DocumentBuilderFactory and the newTransformer method of a TransformerFactory will
            // be thread safe without side effects. This means that an application programmer should expect to be able to create parser
            // instances in multiple threads at once from a shared factory without side effects or problems.
            return s_saxParserFactory.newSAXParser().getParser();
            //if(logger.isInfoEnabled()) logger.info( "xml: 2 Using parser of class: " + p.getClass().getName());
            // return p;
        } catch (Throwable t) {
            logger.error("xml: failed creating saxparser, trying the old (non-jaxp) method", t);
        }
        // try a few other parsers (jclark CommentDriver the default)
        {
            String parserClass = System.getProperty("com.jclark.xsl.sax.parser");
            if (parserClass == null) {
                parserClass = System.getProperty("org.xml.sax.parser");
            }
            if (parserClass == null) {
                parserClass = "com.jclark.xml.sax.CommentDriver";
            }
            //if(logger.isInfoEnabled()) logger.info( "xml: SaxParserClass is " + parserClass);
            try {
                return ((Class<Parser>) Class.forName(parserClass)).newInstance();
                //TODO: OVERRIDE THIS TO WORK AROUND STACK OVERFLOW dos
                // p.setDocumentHandler();
                //if(logger.isInfoEnabled()) logger.info( "xml: 3 Using parser of class: " + parserClass);
                //return p;
            } catch (Exception e) {
                Debug.assertMsg(logger, false, "xml: failed getting parser", e);
            }
            return null;
        }
    }

    /**
     * converts a hashtable to an xml row.  xml escapes data (unless it starts with <!cdata[) but not tag names
     */
    public static StringBuffer convertMapToNameValueXML(String tag, String rowTag, Map<String, String> map, StringBuffer buf) {
        if (buf == null) {
            buf = new StringBuffer();
        }
        if (map == null) {
            return buf;
        }
        if (!IPStringUtil.isEmpty(tag)) {
            XMLUtil.openTag(buf, tag);
        }

        for (Map.Entry<String, String> entry : map.entrySet()) {
            String name = entry.getKey();
            String value = entry.getValue();
            if (!value.startsWith("<![CDATA[")) {
                value = XMLUtil.getEscapedXML(value);
            }
            XMLUtil.openTag(buf, rowTag);
            buf.append("<NAME>").append(name).append("</NAME>\n");
            buf.append("<VALUE>").append(value).append("</VALUE>\n");
            XMLUtil.closeTag(buf, rowTag);
        }
        if (!IPStringUtil.isEmpty(tag)) {
            XMLUtil.closeTag(buf, tag);
        }
        return buf;
    }

    /**
     * converts a hashtable to an xml row.  xml escapes data (unless it starts with <!cdata[)
     * but not tag names
     */
    public static StringBuffer convertHashtableToXML(String tag, Map<String, String> map, boolean closeTag, StringBuffer buf) {
        if (buf == null) {
            buf = new StringBuffer();
        }
        if (map == null) {
            return buf;
        }
        if (tag != null) {
            XMLUtil.openTag(buf, tag);
        }
        for (Map.Entry<String, String> entry : map.entrySet()) {
            String name = entry.getKey();
            String val = entry.getValue();
            addFieldFromObject(buf, name, val);
        }
        if (tag != null && closeTag) {
            XMLUtil.closeTag(buf, tag);
        }
        return buf;
    }

    /**
     * Add a single element based on an object.
     * <p>
     * Note:
     * - All values are XMLEscaped except;
     * - OIDs
     * - Values that are wrapped as CDATA
     * <p>
     * - This method is currently only used by PersistenceUtil.java when getting
     * XML and Data from a Resultset.
     */
    public static <T extends Appendable> T addFieldFromObject(T buf, String tag, Object val) {
        try {
            Object value = val instanceof String[] ? ((Object[]) val)[0] : val;
            int len = tag.length();
            String s = null;
            if (val == null) {
            } else if (val instanceof Double || val instanceof Float) {
                // MySQL was summing(ints) and getting double (fair enough).
                // however the value was x.0 and we want x
                double d;
                if (val instanceof Float) {
                    d = ((Float) val).doubleValue();
                } else {
                    d = ((Double) val).doubleValue();
                }
                s = Double.toString(d);
                if (s.endsWith(".0")) {
                    s = s.substring(0, s.length() - 2);
                }
            } else {
                s = val.toString();
            }
            boolean skipXMLEscaping = s == null || s.toString().startsWith("<![CDATA[") || !(value instanceof String) || !ResultSetInfo.doesFieldNeedXMLEscaping(tag);

            if (IPStringUtil.isEmpty(s)) {
                buf.append('<').append(tag).append("/>");
                return buf;
            }
            if (!skipXMLEscaping) {
                s = XMLUtil.getEscapedXML(s);
            }
            buf.append('<').append(tag).append('>').append(s).append('<').append('/').append(tag).append('>').append('\n');
            return buf;
        } catch (IOException e) {
            throw UnexpectedError.getRuntimeException("Unable to append", e, true);
        }
    }

    static final int CDATA_START_LEN = "<![CDATA[".length();
    static final int CDATA_END_LEN = "]]>".length();

    public static String stripCDATA(String stringWithCDATA) {
        if (IPStringUtil.isEmpty(stringWithCDATA)) {
            return stringWithCDATA;
        }
        if (!stringWithCDATA.startsWith("<![CDATA[")) {
            return stringWithCDATA;
        }
        int message_end = stringWithCDATA.length() - CDATA_END_LEN;
        if (message_end < CDATA_START_LEN) {
            return stringWithCDATA;
        }
        String stripped = CDATA_START_LEN == message_end ? "" : stringWithCDATA.substring(CDATA_START_LEN, message_end);
        return stripped;
    }

    public static String stripCDATAFromTag(String xml, String tagName) {
        if (IPStringUtil.isEmpty(xml)) {
            return xml;
        }
        int tagStart = xml.indexOf("<" + tagName + ">");
        Debug.assertMsg(logger, tagStart > -1, "Tag not found in XML");
        tagStart += tagName.length() + 2;
        int tagEnd = xml.indexOf("</" + tagName + ">", tagStart);
        StringBuffer processedString = new StringBuffer();
        processedString.append(xml.substring(0, tagStart));
        processedString.append(xml.substring(tagStart + CDATA_START_LEN, tagEnd - CDATA_END_LEN));
        processedString.append(xml.substring(tagEnd));
        return processedString.toString();
    }

    /**
     * Wrap the xml fragment indicated by 'tagName' in a CDATA.
     * <p>
     * Note:
     * - This can be used to pass XML fragments to the stylesheets and
     * allow them to be displayed unmodified.
     * - This is required because of the xsl:copy-of habit of formating XML to
     * its liking whenwe're trying to display the xml fragment.
     * - The xml must contain a separate end tag e.g. <TEST></TEST> and not a
     * single e.g. <TEST/>. Makes sense because otherwise what are you trying
     * to enclose with the CDATA ?
     */
    public static StringBuffer wrapXMLFragmentInCDATA(String xml, String tagName) {
        // Validate the input arguments
        Debug.assertMsg(logger, xml != null, "Null xml passed!");
        Debug.assertMsg(logger, tagName != null, "Null tagName passed!");
        if (xml.length() == 0) {
            return new StringBuffer(xml);
        }

        // Identify the start of the fragment for this tag
        int tagStart = xml.indexOf("<" + tagName + ">");
        if (tagStart < 0) {
            return new StringBuffer(xml);
        }
        tagStart += tagName.length() + 2;

        // Identify the end of the fragment for this tag
        int tagEnd = xml.indexOf("</" + tagName + ">", tagStart);
        // No separate end tag so return unchanged
        if (tagEnd < 0) {
            new StringBuffer(xml);
        }

        // Compose the new version of the xml
        StringBuffer processedString = new StringBuffer();
        processedString.append(xml.substring(0, tagStart));
        processedString.append("<![CDATA[");
        processedString.append(getCDATACompatibleString(xml.substring(tagStart, tagEnd)));
        processedString.append("]]>");
        processedString.append(xml.substring(tagEnd));
        return processedString;
    }

    /**
     * output an error based on a document, line col.  good for saxparseerrors or transformationexceptions
     * inputDocumentSystemId may differ from documentSystem id, if say the error is in an import.
     */
    public static StringBuffer getDocumentSyntaxErrorDetail(Throwable originalException, String errorMessage, String documentSystemId, int lineNumber, int columnNumber, String input, String inputDocumentSystemId, boolean disablehtml, boolean showTextBeforeErrorLine) {
        StringBuffer error = new StringBuffer();
        error.append("Error: '" + errorMessage + "', ");
        //String systemId = e.getSystemId();
        //int lineNumber = e.getLineNumber();
        boolean hasOutput = false;
        if (documentSystemId != null) {
            error.append("File: '" + documentSystemId + "'");
            hasOutput = true;
        }
        if (lineNumber >= 0) {
            if (hasOutput) {
                error.append(", ");
            }
            hasOutput = true;
            error.append("line: " + lineNumber);
        }
        if (columnNumber >= 0) {
            if (hasOutput) {
                error.append(", ");
            }
            hasOutput = true;
            error.append("column: " + columnNumber);
        }
        if (hasOutput) {
            error.append('\n');
        }
        if (input != null) {
            LineNumberReader lnr = new LineNumberReader(new StringReader(input));
            int i = lineNumber - (showTextBeforeErrorLine ? 6 : 0);
            try {
                String line = "";
                while (--i > -1 && line != null) {
                    line = lnr.readLine();
                }
                for (int errorLineCount = 0; errorLineCount < 8; errorLineCount++) {
                    addErrorLine(error, lnr, disablehtml);
                }
            } catch (IOException ignore) {
            }
        }
        //if(includeStackDump) {
        //error = new StringBuffer(Debug.getErrorDetail(error.toString(), originalException));
        //}
        return error;
    }

    private static void addErrorLine(Appendable buf, LineNumberReader lnr, boolean disableHTML) throws IOException {
        try {
            int lineNum = lnr.getLineNumber() + 1;
            String line = lnr.readLine();
            if (line == null) {
                return;
            }
            buf.append("\n" + lineNum + ":  " + (disableHTML ? HtmlTextMassager.disableHtml(line) : line));
        } catch (IOException e) {
            throw UnexpectedError.getRuntimeException("Unable to append", e, true);
        }
    }

    /**
     * Removes any occurrences of a specified xml node, if any of nodes below it contain the unqique string
     *
     * @param xml
     * @param entityName
     * @param uniqueString
     * @return
     * @throws ParserConfigurationException
     * @throws IOException
     * @throws SAXException
     */
    public static String removeTagContainingString(String xml, String tagName, String uniqueString) throws ParserConfigurationException, IOException, SAXException, TransformerException {
        DocumentBuilder db = XMLUtil.getDocumentBuilderFactory().newDocumentBuilder();
        Document doc = db.parse(new ByteArrayInputStream(xml.getBytes()));
        removeTagContainingString(doc, tagName, uniqueString);
        return createStringFromDocument(doc);

    }

    public static String createStringFromDocument(Document doc) throws TransformerException {
        Transformer trans = XMLUtil.getTransformerFactory().newTransformer();
        return createStringFromDocument(doc, trans);
    }

    public static String createStringFromDocument(Document doc, Transformer trans) throws TransformerException {
        StringWriter sw = new StringWriter();
        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(sw);
        trans.transform(source, result);
        return sw.toString();
    }

    public static Transformer getXMLFormattingTransformer() throws TransformerConfigurationException {
        String xmlStylesheet = "<?xml version='1.0' encoding='UTF-8'?>" + "<xsl:stylesheet xmlns:xsl='http://www.w3.org/1999/XSL/Transform' version='1.0'>" + " <xsl:output method='xml' version='1.0' encoding='UTF-8' indent='yes' omit-xml-declaration='yes'/>" + " <xsl:template match='node()'>" + " <xsl:copy>" + " <xsl:apply-templates select='node()'/>" + " </xsl:copy>" + " </xsl:template>" + "</xsl:stylesheet>";
        return XMLUtil.getTransformerFactory().newTransformer(new StreamSource(new StringReader(xmlStylesheet)));
    }

    private static TransformerFactory s_transformerFactory;

    public static TransformerFactory getTransformerFactory() {
        if (s_transformerFactory == null) {
            TransformerFactory t = TransformerFactory.newInstance();
            s_transformerFactory = t;
        }
        return s_transformerFactory;
    }

    /**
     * This will remove an entire tag, if anywhere within is contained the unique string supplied.  It could be in
     * an attribute, a child element name, a text node, anything.  Good for removing elements from the servlet config xml
     *
     * @param doc          The document to remove the tag from
     * @param tagName      The tag name to remove
     * @param uniqueString The string to search for within the tag's hierarchy
     */
    public static void removeTagContainingString(Document doc, String tagName, String uniqueString) {
        removeTagContainingString0(doc, tagName, uniqueString);
    }

    private static void removeTagContainingString0(Node parentNode, String tagName, String uniqueString) {

        NodeList list = parentNode.getChildNodes();
        outer:
        for (int i = 0; i < list.getLength(); i++) {
            Node node = list.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                if (node.getNodeName().equals(tagName)) {

                    if (hasTextInHierarchy(node, uniqueString)) {
                        parentNode.removeChild(node);
                        continue outer;
                    }
                }
                removeTagContainingString0(node, tagName, uniqueString);
            }
        }

    }

    /**
     * Looks for occurrences of an extact string occurrence in any attribute name and values for the current node,
     * or ANY occurrences of the text in any child nodes
     *
     * @param parentNode   The node to check
     * @param uniqueString The unique string to check.  Only checks for exact, case sensitive equality of a node name or value
     * @return
     */
    private static boolean hasTextInHierarchy(Node parentNode, String uniqueString) {

        //Check the attributes
        NamedNodeMap attrs = parentNode.getAttributes();
        if (attrs != null) {
            for (int j = 0; j < attrs.getLength(); j++) {
                Node attr = attrs.item(j);
                if (uniqueString.equals(attr.getNodeName()) || uniqueString.equals(attr.getNodeValue())) {
                    return true;
                }
            }
        }
        //check the child nodes
        NodeList list = parentNode.getChildNodes();
        if (list != null) {
            for (int i = 0; i < list.getLength(); i++) {
                Node node = list.item(i);

                //do their names or values match
                if (uniqueString.equals(node.getNodeName()) || uniqueString.equals(node.getNodeValue())) {
                    return true;
                }

                //do any of their child nodes match
                if (hasTextInHierarchy(node, uniqueString)) {
                    return true;
                }
            }
        }

        //nothing matched in this hierarchy so return false
        return false;
    }

    public static Document createDocumentFromString(String xml) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilder db = XMLUtil.getDocumentBuilderFactory().newDocumentBuilder();
        return db.parse(new ByteArrayInputStream(xml.getBytes()));
    }

    public static class XMLValidationError extends NarrativeException {
        public XMLValidationError(String error) {
            super(error);
        }
    }

    /**
     * A utility method to validate that the supplied xml content is valid.
     *
     * @throws XMLValidationError if xml is invalid
     * @xmlContent Mandatory. A valid document or an xml fragment.
     */
    public static void checkXMLIsWellFormed(String xmlContent, String nameOfXML, boolean nameSpacesAllowed, boolean isFragmentOnly) throws XMLValidationError {
        if (IPStringUtil.isEmpty(xmlContent)) {
            return;
        }

        // If we're only validating an xml fragment then do we need to wrap in the std xml prefix ?
        if (isFragmentOnly && (!xmlContent.startsWith("<?xml"))) {
            xmlContent = getXMLPrefix(null).toString() + "<DOC type=\"check_xml\">" + xmlContent + "</DOC>";
        }

        // Attempt to parse the XML
        try {
            StringReader sr = new StringReader(xmlContent);
            InputSource is = new InputSource(sr);
            //com.jclark.xml.sax.Driver parser = new com.jclark.xml.sax.Driver();
            Parser parser = XMLUtil.createParser();
            parser.setDocumentHandler(new NameSpaceValidatingHandler(nameSpacesAllowed));
            parser.parse(is);

        } catch (SAXParseException ex) {
            String docError = getDocumentSyntaxErrorDetail(ex, ex.getMessage(), ex.getSystemId(), ex.getLineNumber(), ex.getColumnNumber(), xmlContent, null, true, true).toString();
            throw new XMLValidationError("Invlid XML. Name/" + nameOfXML + " Error/" + docError);
        } catch (Throwable ex) {
            throw new XMLValidationError("Invlid XML. Name/" + nameOfXML + " Error/" + ex.getMessage());
        }
    }

    public static org.w3c.dom.Element getRootElementFromCommandLineArg(String args[], int index, String argName, Object objectThatWillBePopulated, boolean isOptional, boolean isAcceptingMultiRows) {
        if (index >= args.length) {
            if (isOptional) {
                return null;
            }
            logger.error("Not enough command line args.  Need " + argName);
            IPUtil.onEndOfApp();
            System.exit(1);
            ;
        }
        String argVal = args[index];
        InputStream is = null;
        //read from std in
        if (argVal.equals("-")) {
            is = System.in;
            String msg = "Reading xml from std in (Ctrl-Z to end).  Expecting (with defaults) ";
            if (isAcceptingMultiRows) {
                msg += " multiple elements of";
            }
            if (objectThatWillBePopulated != null) {
                msg += ": " + getObjectAsXMLElementWithAttributes(argName, objectThatWillBePopulated);
            }
            if (logger.isInfoEnabled()) {
                logger.info(msg);
            }
        } else if (argVal.startsWith("<")) {
            is = new ByteArrayInputStream(argVal.getBytes());
            if (logger.isInfoEnabled()) {
                logger.info("arg=" + argVal);
            }
        } else {
            try {
                is = new FileInputStream(argVal);
            } catch (FileNotFoundException fnf) {
                Debug.assertMsg(logger, false, "Could not find " + argVal, fnf);
            }
        }
        String xml = IPIOUtil.getStringFromStreamWithUnknownEncoding(is, argName);
        return getRootElementFromString(xml, argName);
    }

    public static org.w3c.dom.Element getRootElementFromString(String xml, String argName) {
        if (IPStringUtil.isEmpty(xml)) {
            return null;
        }
        XMLUtil.checkXMLIsWellFormed(xml, argName, false, false);
        try {
            DocumentBuilder builder = XMLUtil.getDocumentBuilderFactory().newDocumentBuilder();
            Document document = builder.parse(new InputSource(new StringReader(xml)));
            org.w3c.dom.Element root = document.getDocumentElement();
            return root;
        } catch (IOException ioex) {
            Debug.assertMsg(logger, false, "Failed reading data for " + argName, ioex);
            return null;
        } catch (org.xml.sax.SAXParseException saxe) {
            Debug.assertMsg(logger, false, XMLUtil.getDocumentSyntaxErrorDetail(saxe, saxe.getMessage(), saxe.getSystemId(), saxe.getLineNumber(), saxe.getColumnNumber(), xml, null, false, true).toString(), saxe);
            return null;
        } catch (org.xml.sax.SAXException saxe) {
            Debug.assertMsg(logger, false, "Failed reading " + argName, saxe);
            return null;
        } catch (javax.xml.parsers.ParserConfigurationException pce) {
            Debug.assertMsg(logger, false, "Failed reading " + argName, pce);
            return null;
        }
    }

    /**
     * get a string that shows the given object represented as an xml element
     */
    public static String getObjectAsXMLElementWithAttributes(String elementName, Object o) {
        return getObjectAsXML(elementName, o, null, null);
    }

    public static String getObjectAsXMLElementWithSubElements(String elementName, Object o) {
        return getObjectAsXMLElementWithSubElements(elementName, o, true);
    }

    public static String getObjectAsXMLElementWithSubElements(String elementName, Object o, boolean closeElementTag) {
        ObjectAsXMLOptions options = ObjectAsXMLOptions.createConservativePublicXMLOptions();
        options.asAttrNotElements = false;
        options.closeElementTag = closeElementTag;
        return getObjectAsXML(elementName, o, options, null);
    }

    public static class ObjectAsXMLOptions {
        /**
         * when true output is like <elementname fielda="x" /> when false, it
         * is like <elementname><FIELDA>x</FIELDA></elementname>
         */
        public boolean asAttrNotElements = true;
        public boolean useUppercaseElementNames = true;
        /**
         * exclude certain field types from the output.
         */
        public boolean excludeFinalFields = true;
        public boolean excludeStaticFields = true;
        public boolean excludePrivateAndProtectedFields = true;
        public boolean setAllFieldsAccessible = false;
        /**
         * e.g. exclude arrays, app defined classes.  include primitives, Strings, java.util.dates
         */
        public boolean includeUnrecognisedTypes = true;
        /**
         * when true, this will usually will result in only 'db' field names coming back
         */
        public boolean includeOnlyLowerCaseFieldNames = false;
        public boolean openElementTag = true;
        public boolean closeElementTag = true;

        public static ObjectAsXMLOptions createConservativePublicXMLOptions() {
            ObjectAsXMLOptions ret = new ObjectAsXMLOptions();
            ret.asAttrNotElements = false;
            ret.includeUnrecognisedTypes = false;
            ret.includeOnlyLowerCaseFieldNames = true;
            return ret;
        }

        public static ObjectAsXMLOptions createDebugOptions() {
            ObjectAsXMLOptions ret = new ObjectAsXMLOptions();
            ret.excludeFinalFields = false;
            ret.excludeStaticFields = false;
            return ret;
        }
    }

    public static String getObjectAsXMLElementIncludingPrivateAndProtectedFields(String elementName, Object o) {
        ObjectAsXMLOptions options = new ObjectAsXMLOptions();
        options.asAttrNotElements = false;
        options.excludePrivateAndProtectedFields = false;
        options.setAllFieldsAccessible = true;
        options.useUppercaseElementNames = false;
        return getObjectAsXML(elementName, o, options, null);
    }

    public static String getObjectAsXML(String elementName, Object o, ObjectAsXMLOptions options, String optionalFieldNames[]) {
        if (options == null) {
            options = new ObjectAsXMLOptions();
        }

        StringBuffer ret = null;
        if (options.openElementTag) {
            ret = new StringBuffer("<" + elementName + (options.asAttrNotElements ? " " : ">"));
        } else {
            ret = new StringBuffer();
            options.asAttrNotElements = false;
        }

        Field fields[] = IPUtil.getDeclaredFields(o.getClass());
        if (options.excludePrivateAndProtectedFields) {
            fields = IPUtil.getPublicFields(fields);
        }
        if (options.excludeFinalFields) {
            fields = IPUtil.getNonFinalFields(fields);
        }
        if (options.excludeStaticFields) {
            fields = IPUtil.getNonStaticFields(fields);
        }
        if (optionalFieldNames != null) {
            optionalFieldNames = IPStringUtil.getSortedStringArray(optionalFieldNames);
        }
        String[] fieldValues = new String[fields.length];
        for (int i = 0; i < fields.length; i++) {
            try {
                Field field = fields[i];
                // skip this field if it's not in the list of names to include.
                if (optionalFieldNames != null && IPStringUtil.getIndexOfStringInArray(optionalFieldNames, field.getName()) < 0) {
                    continue;
                }
                // if we're only including lowercase fields and this field isn't a strictly lowercase field name, skip it.
                if (options.includeOnlyLowerCaseFieldNames && !IPUtil.isEqual(field.getName(), field.getName().toLowerCase())) {
                    continue;
                }
                if (options.setAllFieldsAccessible) {
                    field.setAccessible(true);
                }
                Object val = field.get(o);
                String valStr = "";
                boolean requiresEscaping = true;
                do {
                    if (val == null) {
                        valStr = null;
                        break;
                    }
                    Class c = val.getClass();
                    if (c.equals(Boolean.class)) {
                        valStr = ((Boolean) val).booleanValue() ? "Y" : "N";
                        requiresEscaping = false;
                    } else if (c.equals(String.class)) {
                        valStr = val.toString();
                    } else if (c.equals(java.sql.Date.class)) {
                        valStr = IPDateUtil.getYYYYMMDDStringFromDate((java.sql.Date) val);
                        requiresEscaping = false;
                    } else if (java.util.Date.class.isAssignableFrom(c)) {
                        valStr = IPDateUtil.getTimestampFromDate((java.util.Date) val).toString();
                        requiresEscaping = false;
                    } else if (c.equals(OID.class)) {
                        valStr = ((OID) val).toString();
                        requiresEscaping = false;
                    } else {
                        if (options.includeUnrecognisedTypes || c.isPrimitive() || c.equals(Integer.class) || c.equals(Character.class) || c.equals(Long.class) || c.equals(Float.class)) {
                            ;
                        } else {
                            continue;
                        }
                        valStr = val.toString();
                    }
                } while (false);
                if (options.asAttrNotElements) {
                    if (valStr != null) {
                        ret = ret.append('\n').append(field.getName()).append("=\"").append(requiresEscaping ? getEscapedXMLForAttribute(valStr) : valStr).append("\"");
                    }
                } else {
                    String fieldName = options.useUppercaseElementNames ? field.getName().toUpperCase() : field.getName();
                    ret = ret.append("\n<" + fieldName + '>' + (valStr == null ? "" : requiresEscaping ? getEscapedXML(valStr) : valStr) + "</" + fieldName + '>');
                }
            } catch (IllegalAccessException iae) {
                ;//ignore
            }
        }
        if (options.closeElementTag) {
            ret.append(options.asAttrNotElements ? "\n/>" : "\n</" + elementName + ">");
        }
        return ret.toString();
    }

    /**
     * I want to pass in some context to the xml setup components.  Not sure what we'll need.  Figure it's easier to
     * have this class than just popping a hashtable on the XMLSetupComponent init method and having to
     * change that everywhere later on.
     * <p>
     * XMLSetupContext uses?  On appupdate setting a site_oid to override the XML site oid.  On appupdate setting
     * a 's_lastRunIfDBVersionLessThan' so we can rerun app updates
     */
    public static class XMLSetupContext {
        public Map<String, Object> contextData = new HashMap<String, Object>();
        //public IPUtil.LongRunningTask taskInfo = new IPUtil.LongRunningTask();
    }

    public interface XMLSetupComponent {
        /**
         * perform any initialization necessary.  If the component wishes to
         * run in its own thread, it returns a runnable.
         *
         * @param resultInfoMessages The method can add any info, warn, or error
         *                           messages here
         */
        Runnable init(Element localConfig, Element globalConfig, XMLSetupContext context);
    }

    /**
     * Replacement for HandlerBase for use when also wanting to validate xml namespaces.
     */
    public static class NameSpaceValidatingHandler extends HandlerBase {
        boolean nameSpaceAllowed = true;

        public NameSpaceValidatingHandler(boolean areNameSpacesAllowed) {
            nameSpaceAllowed = areNameSpacesAllowed;
        }

        public void startElement(String tagName, AttributeList attributes) throws SAXException {
            super.startElement(tagName, attributes);
            if ((!nameSpaceAllowed) && (tagName.indexOf(":") >= 0)) {
                throw new SAXException("Namespace not allowed");
            }
        }
    }

    public static String getElementAsString(Element e, boolean trueCopy) {
        return getElementAsString(e, trueCopy, false);
    }

    public static String getElementAsString(Element e, boolean trueCopy, boolean makePretty) {
        StringBuffer sb = new StringBuffer();
        getElementAsString0(sb, e, trueCopy, makePretty, 0);
        return sb.toString();
    }

    private static String getElementAsString0(Appendable sb, Element e, boolean trueCopy, boolean makePretty, int level) {

        try {
            Element child = e;
            if (makePretty) {
                sb.append("\n");
                IPStringUtil.repeatString(sb, "\t", level);
            }
            sb.append("<" + child.getNodeName());
            NamedNodeMap nnm = child.getAttributes();
            int nnml = nnm.getLength();
            for (int nnmi = 0; nnmi < nnml; nnmi++) {
                Node attr = nnm.item(nnmi);
                if (makePretty) {
                    sb.append("\n");
                    IPStringUtil.repeatString(sb, "\t", level + 2);
                } else {
                    sb.append(' ');
                }
                sb.append(attr.getNodeName() + "=\"");
                String attrValue = ((Element) child).getAttribute(attr.getNodeName());
                // must get escaped XML for the attribute value if using trueCopy to
                // prevent lone & in urls like "&s=1".  getting escaped xml gives "&amp;s=1"
                if (trueCopy) {
                    attrValue = getEscapedXMLForAttribute(attrValue);
                }
                sb.append(attrValue + '"');
            }
            String nodeContent = getNodeContentAsString0(child, "", trueCopy, makePretty, level + 1);
            if (IPStringUtil.isEmpty(nodeContent)) {
                if (makePretty && nnml > 0) {
                    sb.append('\n');
                    IPStringUtil.repeatString(sb, "\t", level);
                }
                sb.append("/>");
            } else {
                sb.append(">").append(nodeContent);
                if (makePretty) {
                    sb.append('\n');
                    IPStringUtil.repeatString(sb, "\t", level);
                }

                sb.append("</").append(child.getNodeName()).append(">");
            }
            if (makePretty && level == 1) {
                sb.append("\n");
            }

            return sb.toString();
        } catch (IOException e1) {
            throw UnexpectedError.getRuntimeException("Unable to append", e1, true);
        }
    }

    /**
     * get the name/value pairs for the attributes on a given element
     * in the form of a Hashtable
     *
     * @param e the element to get all attributes for
     * @return a Hashtable containing all of the name/value pairs for attributes on element e
     */
    public static Hashtable getAttributeNameValuePairsFromElement(Element e) {
        Hashtable nameValuePairs = new Hashtable();
        NamedNodeMap nnm = e.getAttributes();
        int nnml = nnm.getLength();
        for (int nnmi = 0; nnmi < nnml; nnmi++) {
            Node attr = nnm.item(nnmi);
            String name = attr.getNodeName();
            String value = e.getAttribute(name);
            nameValuePairs.put(name, value);
        }
        return nameValuePairs;
    }

    /**
     * gets the data in the node.  handles cases where the node contains html elements,
     * attributes or cdata
     *
     * @param trueCopy. If true then will leave xml Entity Refs and CDATA blocks intact. This
     *                  is used when cloning sites etc. and wanting an exact copy of a node.
     */
    public static String getNodeContentAsString(Node n, String defaultValue, boolean trueCopy) {
        return getNodeContentAsString0(n, defaultValue, trueCopy, false, 0);
    }

    private static String getNodeContentAsString0(Node n, String defaultValue, boolean trueCopy, boolean makePretty, int depth) {
        //Node n = e.getFirstChild();
        if (n == null) {
            return defaultValue;
        }
        NodeList nl = n.getChildNodes();
        if (nl == null) {
            return defaultValue;
        }
        int len = nl.getLength();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < len; i++) {
            Node child = nl.item(i);
            if (child == null) {
                continue;
            }
            short childType = child.getNodeType();
            switch (childType) {
                case Node.ELEMENT_NODE: {
                    getElementAsString0(sb, (Element) child, trueCopy, makePretty, depth);
                    break;
                }
                case Node.ENTITY_REFERENCE_NODE: {
                    // If we don't want XML Entity refs resolved then avoid the
                    // default DOM api behavior.
                    if (!trueCopy) {
                        // The content is in the TEXT node under it
                        sb.append(getNodeContentAsString0(child, "", trueCopy, makePretty, depth + 1));
                    } else {
                        // Preserve the XML Entity Reference
                        sb.append("&" + child.getNodeName() + ";");
                    }
                    break;
                }
                case Node.CDATA_SECTION_NODE: {
                    CharacterData cdata = (CharacterData) child;
                    String s = cdata.getData();
                    // Just the CDATA contents ?
                    if (!trueCopy) {
                        sb.append(s);
                    } else {
                        // Leave as a CDATA
                        sb.append("<![CDATA[" + s + "]]>");
                        if (makePretty) {
                            sb.append('\n');
                        }
                    }
                    break;
                }
                case Node.TEXT_NODE: {
                    Text text = (Text) child;
                    String s = text.getData();
                    // must get escaped copy of the text node for a true copy in order to have
                    // &amp;copy; instead of just &copy;
                    if (makePretty) {
                        if (s.replaceAll("\n", "").replaceAll(" ", "").replaceAll("\t", "").length() == 0) {
                            break;
                        }
                    }
                    if (trueCopy) {
                        s = getEscapedXML(s);
                    }
                    sb.append(s);
                    break;
                }
                case Node.COMMENT_NODE: {
                    Comment comment = (Comment) child;
                    String s = comment.getData();
                    if (trueCopy) {
                        s = getEscapedXML(s);
                    }

                    if (makePretty) {
                        String tabs = IPStringUtil.repeatString("\t", depth);
                        sb.append('\n');
                        sb.append(tabs);
                        s.replaceAll("\t", "");
                        s = s.replaceAll("\n", "\n" + tabs);
                    }
                    sb.append("<!-- " + s + " -->");
                    break;
                }
                default:
                    continue;
            }
        }
        ;
        if (sb.length() < 1) {
            return defaultValue;
        }
        return sb.toString();
    }

    /**
     * get the attibutes from an element.  eg. getValueFromElement("<SITES site_oid="xxx" />"
     * , site_oid, default) returns xxx
     */
    public static String getAttributeFromElement(Element e, String attributeName, String defaultVal) {
        if (e == null) {
            return defaultVal;
        }
        // curiously getAttribute does not return null if the attribute does not
        // exist, hence we explicitly try getting the attr object first
        Attr a = e.getAttributeNode(attributeName);
        if (a == null) {
            return defaultVal;
        }
        String s = e.getAttribute(attributeName);
        if (s == null) {
            return defaultVal;
        }
        return s;
    }

    /**
     * bad extendata parameter name.  apperror (cf. runtime) since the admin will probably
     * need to see and correct the problem
     */
    public static class ExtendataParameterNameError extends NarrativeException {
        public String badName;

        public ExtendataParameterNameError(String elementName) {
            super("Bad extendata: " + elementName);
            badName = elementName;
        }

    }

    public static void insertXMLIntoDoc(StringBuffer xmlContent, String xmlToInsert) {
        if (IPStringUtil.isEmpty(xmlToInsert)) {
            return;
        }
        // Insert the client browser summary information.
        int closingDocIndex = xmlContent.lastIndexOf("</DOC>");
        if (closingDocIndex < 0) {
            Debug.assertMsg(logger, false, "Expecting '</DOC>' in this xml: " + IPStringUtil.getStringAfterTruncatingToPosition(xmlContent.toString(), xmlContent.length() - 100));
        }
        xmlContent.insert(closingDocIndex, xmlToInsert);
    }

    /**
     * converts element[1] into an element name (element) and index (1) and attribute.
     */
    public static class WPathFragment {
        public String fragmentName;
        public int elementIndex = 0;
        public static int WP_ELEMENT = 0;
        public static int WP_ATTRIBUTE = 1;
        public int wpType = WP_ELEMENT;
        WPathFragment nextFragment = null;

        public WPathFragment(String _sWPathFragmentToParse, String originalWPath) {
            String remainingWPathAfterThisFragment = "";
            fragmentName = _sWPathFragmentToParse;
            if (fragmentName == null) {
                throw new ExtendataParameterNameError(originalWPath);
            }
            // figure out the end of the current fragment (/ delimiter)
            do {
                int indexOfNextFragment = fragmentName.indexOf('/');
                // we're the last fragment
                if (indexOfNextFragment < 0) {
                    break;
                }
                // nothing after the /
                if (fragmentName.length() - 1 == indexOfNextFragment) {
                    throw new ExtendataParameterNameError(originalWPath);
                }
                // remember the remaining bit
                remainingWPathAfterThisFragment = fragmentName.substring(indexOfNextFragment + 1);
                // and set the bit we have to deal with
                fragmentName = fragmentName.substring(0, indexOfNextFragment);
            } while (false);
            // empty
            if (IPStringUtil.isEmpty(fragmentName)) {
                throw new ExtendataParameterNameError(originalWPath);
            }
            // are we an attribute?
            do {
                int attrIndex = fragmentName.indexOf('@');
                if (attrIndex < 0) {
                    break;
                }
                // attribute index not at start, err
                if (attrIndex > 0) {
                    throw new ExtendataParameterNameError(originalWPath);
                }
                // we're an attribute
                wpType = WP_ATTRIBUTE;
                // get rid of the @
                fragmentName = fragmentName.substring(1);
                // yet more empty checking
                if (IPStringUtil.isEmpty(fragmentName)) {
                    throw new ExtendataParameterNameError(originalWPath);
                }
            } while (false);
            do {
                // indexes on elemens only (not attributes)
                if (wpType != WP_ELEMENT) {
                    break;
                }
                int leftSB = fragmentName.indexOf('[');
                if (leftSB < 0) {
                    break;
                }
                // element name starts with index? err
                if (leftSB == 0) {
                    throw new ExtendataParameterNameError(originalWPath);
                }
                int rightSB = fragmentName.indexOf(']', leftSB);
                // left but no right?
                if (rightSB < 0) {
                    throw new ExtendataParameterNameError(originalWPath);
                }
                // element not ending with right square bracket? err
                if (rightSB != fragmentName.length() - 1) {
                    throw new ExtendataParameterNameError(originalWPath);
                }
                try {
                    String num = fragmentName.substring(leftSB + 1, rightSB);
                    // string index is 1 based.  But this is atypical for java, so
                    // we're going to keek the java one 0 based
                    elementIndex = Integer.parseInt(num) - 1;
                    if (elementIndex < 0) {
                        throw new ExtendataParameterNameError(originalWPath);
                    }
                } catch (NumberFormatException nfe) {
                    throw new ExtendataParameterNameError(originalWPath);
                }
                fragmentName = fragmentName.substring(0, leftSB);
                // yet more empty checking
                if (IPStringUtil.isEmpty(fragmentName)) {
                    throw new ExtendataParameterNameError(originalWPath);
                }
            } while (false);
            {
                // last few checks..
                // don't want more ['s
                if (fragmentName.indexOf(']') > -1) {
                    throw new ExtendataParameterNameError(originalWPath);
                }
                if (fragmentName.indexOf('[') > -1) {
                    throw new ExtendataParameterNameError(originalWPath);
                }
            }
            // get the other fragments (recursively)
            if (!IPStringUtil.isEmpty(remainingWPathAfterThisFragment)) {
                nextFragment = new WPathFragment(remainingWPathAfterThisFragment, originalWPath);
            }
        }
    }

    public static String getStringfromXmlFileAndPath(String fileName, String xpathExpression) {
        try {
            DocumentBuilderFactory dbf = XMLUtil.getDocumentBuilderFactory();
            Document doc = dbf.newDocumentBuilder().parse(new File(fileName));

            XPathExpression expr = XPathFactory.newInstance().newXPath().compile(xpathExpression);
            return (String) expr.evaluate(doc, XPathConstants.STRING);
        } catch (Exception ex) {
            throw UnexpectedError.getRuntimeException("Error getting node from file " + fileName + " with expression {" + xpathExpression + "}", ex);
        }
    }

    public static Boolean getBoolean(XPath xPath, Node node, String value) {
        try {
            return (Boolean) xPath.evaluate(value, node, XPathConstants.BOOLEAN);
        } catch (XPathExpressionException ex) {
            if (logger.isWarnEnabled()) {
                logger.warn("Could not Boolean get " + value + " from node " + node.getNodeName(), ex);
            }
        }

        return null;
    }

    public static Double getDouble(XPath xPath, Node node, String value) {
        try {
            return (Double) xPath.evaluate(value, node, XPathConstants.NUMBER);
        } catch (XPathExpressionException ex) {
            if (logger.isWarnEnabled()) {
                logger.warn("Could not Number get " + value + " from node " + node.getNodeName(), ex);
            }
        }

        return null;
    }

    public static Integer getInteger(XPath xPath, Node node, String value) {
        Double doubleVal = getDouble(xPath, node, value);

        return doubleVal == null ? null : doubleVal.intValue();
    }

    public static String getString(XPath xPath, Node node, String value) {
        try {
            return (String) xPath.evaluate(value, node, XPathConstants.STRING);
        } catch (XPathExpressionException ex) {
            if (logger.isWarnEnabled()) {
                logger.warn("Could not String get " + value + " from node " + node.getNodeName(), ex);
            }
        }

        return null;
    }

    public static OID getOid(XPath xPath, Node node, String value) {
        return OID.valueOf(getString(xPath, node, value));
    }

    public static Node getNode(XPath xPath, Node node, String value) {
        try {
            return (Node) xPath.evaluate(value, node, XPathConstants.NODE);
        } catch (XPathExpressionException ex) {
            if (logger.isWarnEnabled()) {
                logger.warn("Could not Node get " + value + " from node " + node.getNodeName(), ex);
            }
        }

        return null;
    }

    public static NodeList getNodeList(XPath xPath, Node node, String value) {
        try {
            return (NodeList) xPath.evaluate(value, node, XPathConstants.NODESET);
        } catch (XPathExpressionException ex) {
            if (logger.isWarnEnabled()) {
                logger.warn("Could not NodeList get " + value + " from node " + node.getNodeName(), ex);
            }
        }

        return null;
    }

    public static Date getDate(XPath xPath, Node node, String value) {
        try {
            //is this performant?
            String dateAsXMLString = getString(xPath, node, value);

            if (IPStringUtil.isEmpty(dateAsXMLString)) {
                return null;
            }

            DatatypeFactory datatypeFactory = DatatypeFactory.newInstance();
            return datatypeFactory.newXMLGregorianCalendar(dateAsXMLString).toGregorianCalendar().getTime();
        } catch (DatatypeConfigurationException ex) {
            if (logger.isWarnEnabled()) {
                logger.warn("Could not parse Date " + value + " from node " + node.getNodeName(), ex);
            }
        }

        return null;
    }

}