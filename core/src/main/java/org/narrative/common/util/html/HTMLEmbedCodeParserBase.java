package org.narrative.common.util.html;

import org.narrative.common.util.NarrativeLogger;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Created by IntelliJ IDEA.
 * User: jonmark
 * Date: 12/14/14
 * Time: 9:37 AM
 */
public abstract class HTMLEmbedCodeParserBase extends HTMLParser {
    private static final NarrativeLogger logger = new NarrativeLogger(HTMLEmbedCodeParserBase.class);

    private BigDecimal width = null;
    private BigDecimal height = null;

    protected abstract boolean continueProcessing();

    protected abstract void addResult(String src, BigDecimal width, BigDecimal height, boolean iframe, String flashvars);

    private final List<String> primaryParsableElements;
    private final List<String> parsableElementsSansObject;

    private final Stack<String> openElements = newStack();
    private final Map<String, String> attributes = newHashMap();

    private String currentParamName;
    private String currentParamValue;

    protected static final String OBJECT__ELEMENT_NAME = "object";
    protected static final String IFRAME__ELEMENT_NAME = "iframe";
    protected static final String EMBED__ELEMENT_NAME = "embed";
    protected static final String IMG__ELEMENT_NAME = "img";
    protected static final String AUDIO__ELEMENT_NAME = "audio";
    protected static final String VIDEO__ELEMENT_NAME = "video";
    protected static final String SOURCE__ELEMENT_NAME = "source";

    private static final String PARAM__ELEMENT_NAME = "param";
    private static final String NAME__PARAM_ATTR = "name";
    private static final String VALUE__PARAM_ATTR = "value";

    private static final String SRC__ATTR_NAME = "src";
    private static final String WIDTH__ATTR_NAME = "width";
    private static final String HEIGHT__ATTR_NAME = "height";
    private static final String FLASHVARS__ATTR_NAME = "flashvars";

    private static final List<String> NON_OBJECT_ATTRIBUTES = Collections.unmodifiableList(Arrays.asList(SRC__ATTR_NAME, WIDTH__ATTR_NAME, HEIGHT__ATTR_NAME));

    private static final Map<String, String> PARAM_NAME_TO_ATTR_NAME;

    static {
        Map<String, String> paramNameToAttrName = newHashMap();
        paramNameToAttrName.put("movie", SRC__ATTR_NAME);
        paramNameToAttrName.put(WIDTH__ATTR_NAME, WIDTH__ATTR_NAME);
        paramNameToAttrName.put(HEIGHT__ATTR_NAME, HEIGHT__ATTR_NAME);
        paramNameToAttrName.put(FLASHVARS__ATTR_NAME, FLASHVARS__ATTR_NAME);

        PARAM_NAME_TO_ATTR_NAME = Collections.unmodifiableMap(paramNameToAttrName);
    }

    protected HTMLEmbedCodeParserBase(String embedCode, List<String> primaryParsableElements) {
        super(embedCode, FragmentType.BODY);

        this.primaryParsableElements = primaryParsableElements;
        assert primaryParsableElements.contains(OBJECT__ELEMENT_NAME) : "Currently the OBJECT element should always be in the list of embeddable elements to look for!";
        List<String> parsableElementsSansObject = newLinkedList();
        for (String element : primaryParsableElements) {
            if (!OBJECT__ELEMENT_NAME.equalsIgnoreCase(element)) {
                parsableElementsSansObject.add(element);
            }
        }

        this.parsableElementsSansObject = Collections.unmodifiableList(parsableElementsSansObject);
    }

    @Override
    protected int startElement(String elementName, boolean isSelfClose) {
        String lowerName = elementName.toLowerCase();
        openElements.push(lowerName);

        return super.startElement(elementName, isSelfClose);
    }

    @Override
    protected int addAttribute(String attributeName, String attributeValue) {
        if (continueProcessing()) {
            String lowerName = attributeName.toLowerCase();

            // jw: if we are inside of a Param element
            if (insideParamElement()) {
                if (isEqual(NAME__PARAM_ATTR, lowerName)) {
                    currentParamName = PARAM_NAME_TO_ATTR_NAME.get(attributeValue.toLowerCase());
                }
                if (isEqual(VALUE__PARAM_ATTR, lowerName)) {
                    currentParamValue = attributeValue;
                }

                // jw: only add the attribute if its not already set
            } else if (insideNonObjectParsableElement() && NON_OBJECT_ATTRIBUTES.contains(lowerName) && !attributes.containsKey(lowerName) && !isEmpty(attributeValue)) {
                attributes.put(lowerName, attributeValue);
            }
        }
        return super.addAttribute(attributeName, attributeValue);
    }

    @Override
    protected int closeElement(String elementName, boolean isSelfClose) {
        String lowerName = elementName.toLowerCase();
        boolean isClosingObjectParam = insideParamElement();

        String poppedElement = openElements.pop();
        assert isEqual(poppedElement, lowerName) : "The closing element should always be the same as the element at the top of the stack";

        if (continueProcessing()) {
            // jw: if we are closing a parsable element and there are no more in the stack then lets try and get all
            //     relevant attributes.
            if (primaryParsableElements.contains(lowerName) && !isParsableObjectStillInStack()) {
                try {
                    if (attributes.containsKey(WIDTH__ATTR_NAME)) {
                        width = new BigDecimal(attributes.get(WIDTH__ATTR_NAME));
                    }
                } catch (NumberFormatException nfe) {
                    if (logger.isWarnEnabled()) {
                        logger.warn("Failed parsing width for embed code.", nfe);
                    }
                }
                try {
                    if (attributes.containsKey(HEIGHT__ATTR_NAME)) {
                        height = new BigDecimal(attributes.get(HEIGHT__ATTR_NAME));
                    }
                } catch (NumberFormatException nfe) {
                    if (logger.isWarnEnabled()) {
                        logger.warn("Failed parsing height for embed code.", nfe);
                    }
                }

                String src = attributes.get(SRC__ATTR_NAME);

                if (!isEmpty(src)) {
                    addResult(src, width, height, isEqual(lowerName, IFRAME__ELEMENT_NAME), attributes.get(FLASHVARS__ATTR_NAME));
                }

                // jw: clear out the attributes, the implementation should be concerned about what to do with the previous
                //     values, not us.
                width = null;
                height = null;
                attributes.clear();

                // if we are closing the embed param then lets add a attribute if we have both a param name and value
            } else if (isClosingObjectParam) {
                // only add a attribute if we parsed both the name and value for the param, and the attributes map does not already have it.
                if (!isEmpty(currentParamName) && !isEmpty(currentParamValue) && !attributes.containsKey(currentParamName)) {
                    attributes.put(currentParamName, currentParamValue);
                }

                // jw: lets just always clear these both just in case!
                currentParamName = null;
                currentParamValue = null;
            }
        }

        return super.closeElement(elementName, isSelfClose);
    }

    private boolean insideParamElement() {
        if (openElements.size() < 2) {
            return false;
        }

        return isEqual(openElements.get(openElements.size() - 1), PARAM__ELEMENT_NAME) && isEqual(openElements.get(openElements.size() - 2), OBJECT__ELEMENT_NAME);
    }

    private boolean insideNonObjectParsableElement() {
        return parsableElementsSansObject.contains(openElements.peek());
    }

    // Determines if any parseable element (iframe, object, embed) are still in the stack
    private boolean isParsableObjectStillInStack() {
        for (String openElement : openElements) {
            if (primaryParsableElements.contains(openElement)) {
                return true;
            }
        }

        return false;
    }
}
