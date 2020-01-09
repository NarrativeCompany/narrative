package org.narrative.common.util.html;

import org.narrative.common.persistence.NameValuePair;
import org.narrative.common.util.IPStringUtil;
import org.narrative.common.util.posting.AnchorConditionalAttribute;
import org.narrative.common.util.posting.AnchorPattern;
import org.narrative.common.util.posting.AnchorTextMassager;
import org.narrative.common.util.posting.GraemlinMassager;
import org.narrative.network.core.master.graemlins.Graemlin;

import java.util.Collection;
import java.util.Map;
import java.util.Stack;

/**
 * Date: Mar 8, 2006
 * Time: 4:33:41 PM
 *
 * @author Brian
 */
public class HTMLEnforcer extends HTMLParser {

    private final Map<String, Collection<String>> supportedElementsToAttributes;
    private final Map<String, Collection<NameValuePair<String>>> elementNamesToAttributeNameValueToEnforce;
    private final Map<String, Collection<NameValuePair<Map<String, String>>>> elementNamesToSubElementToEnforce;
    private final Map<String, Map<String, Collection<String>>> elementNamesToAttributesWithLimitedValues;
    private final Stack<String> currentAnchorHrefValue = new Stack<>();
    private final Stack<ElementInfo> elements = new Stack<>();

    private final Options options;
    private Collection<String> attributesSupportedByCurrentElement;

    private HTMLEnforcer(String html, Options options, HTMLParser.FragmentType fragmentType) {
        this(html, options, fragmentType, null, null, null, null);
    }

    public HTMLEnforcer(String html, Options options, HTMLParser.FragmentType fragmentType, Map<String, Collection<String>> supportedElementsToAttributes, Map<String, Collection<NameValuePair<String>>> elementNamesToAttributeNameValueToEnforce, Map<String, Collection<NameValuePair<Map<String, String>>>> elementNamesToSubElementToEnforce, Map<String, Map<String, Collection<String>>> elementNamesToAttributesWithLimitedValues) {
        super(html, fragmentType);
        this.options = options;
        this.supportedElementsToAttributes = supportedElementsToAttributes;
        this.elementNamesToAttributeNameValueToEnforce = elementNamesToAttributeNameValueToEnforce;
        this.elementNamesToSubElementToEnforce = elementNamesToSubElementToEnforce;
        this.elementNamesToAttributesWithLimitedValues = elementNamesToAttributesWithLimitedValues;
    }

    protected boolean isElementValid(String elementNameLowercase) {
        return options.isAllowAnyHtml || supportedElementsToAttributes.containsKey(elementNameLowercase);
    }

    protected boolean isAttributeSupportedByElement(String elementNameLowercase, String attributeNameLowercase) {
        return options.isAllowAnyHtml || (attributesSupportedByCurrentElement != null && attributesSupportedByCurrentElement.contains(attributeNameLowercase));
    }

    protected int startElement(String elementName, boolean isSelfClose) {
        String elementNameLowercase = elementName.toLowerCase();
        boolean isCurrentElementValid = isElementValid(elementNameLowercase);
        escapeCurrentElement = !isCurrentElementValid;
        elements.push(new ElementInfo(elementName, escapeCurrentElement));
        int ret = super.startElement(elementName, isSelfClose);
        // bl: since we always do a stack pop for anchor elements in closeElement, we need to do the same here.
        // just use an empty string since we don't yet know the href value (if any).
        if ("a".equalsIgnoreCase(elementName)) {
            currentAnchorHrefValue.push("");
        }
        if (isCurrentElementValid) {
            attributesSupportedByCurrentElement = options.isAllowAnyHtml ? null : supportedElementsToAttributes.get(elementNameLowercase);
            Collection<NameValuePair<String>> attributeNameValuesToEnforce = options.isAllowAnyHtml ? null : elementNamesToAttributeNameValueToEnforce.get(elementNameLowercase);
            if (attributeNameValuesToEnforce != null) {
                for (NameValuePair<String> attributeNameValueToEnforce : attributeNameValuesToEnforce) {
                    // since we're enforcing this attribute, don't check valid attributes here.
                    addAttribute(attributeNameValueToEnforce.getName(), attributeNameValueToEnforce.getValue(), false);
                }
            }
        }

        return ret;
    }

    protected int addAttribute(String attributeName, String attributeValue) {
        return addAttribute(attributeName, attributeValue, true);
    }

    private int addAttribute(String attributeName, String attributeValue, boolean checkValidAttributes) {
        if (!elements.empty()) {
            ElementInfo elementInfo = elements.peek();

            if (elementInfo.isExclude()) {
                //sb: we're already excluding this element, so don't try to add attributes to it.
                return 0;
            }

            if (!elementInfo.isEscape()) {
                String currentElement = elementInfo.getName().toLowerCase();

                if ("a".equalsIgnoreCase(currentElement) && attributeName.equalsIgnoreCase("href")) {
                    // bl: remove the empty attribute that was already in the stack and set it to the new value.
                    currentAnchorHrefValue.pop();
                    currentAnchorHrefValue.push(attributeValue);
                }
                if (!checkValidAttributes) {
                    return super.addAttribute(attributeName, attributeValue);
                }
                String attributeNameLowercase = attributeName.toLowerCase();
                if (isAttributeSupportedByElement(currentElement, attributeNameLowercase)) {
                    Map<String, Collection<String>> attributesWithLimitedValues = options.isAllowAnyHtml ? null : elementNamesToAttributesWithLimitedValues.get(currentElement);
                    if (attributesWithLimitedValues != null && attributesWithLimitedValues.containsKey(attributeNameLowercase)) {
                        // for this attribute, only allow one of the specified values.
                        if (attributesWithLimitedValues.get(attributeNameLowercase).contains(attributeValue.toLowerCase())) {
                            return super.addAttribute(attributeName, attributeValue);
                        }
                        // since the attribute value supplied wasn't supported, it shouldn't be included
                        int start = buffer.lastIndexOf("<");
                        if (start > -1) {
                            int end = buffer.length();
                            buffer.replace(start, end, "");
                        }
                        elementInfo.setExclude(true);
                    } else {
                        return super.addAttribute(attributeName, attributeValue);
                    }
                }
            } else {
                // bl: if the current element isn't valid, then it will have been escaped, in which
                // case there is no harm in just outputting the attribute text.
                super.addAttribute(attributeName, attributeValue);
            }
        }
        return 0;
    }

    protected int endStartElement() {
        ElementInfo elementInfo = !elements.empty() ? elements.peek() : null;
        int ret = 0;
        // bl: if this element is being excluded, we don't want to end the start element or else we'll be left
        // with a dangling '>' in the output!
        if(elementInfo==null || !elementInfo.isExclude) {
            ret = super.endStartElement();
        }
        if (elementInfo!=null && !elementInfo.isEscape()) {
            Collection<NameValuePair<Map<String, String>>> subElementsToEnforce = options.isAllowAnyHtml ? null : elementNamesToSubElementToEnforce.get(elementInfo.getName().toLowerCase());
            if (subElementsToEnforce != null) {
                for (NameValuePair<Map<String, String>> subElementToEnforce : subElementsToEnforce) {
                    String subElementName = subElementToEnforce.getName();
                    // bl: currently only supporting non-self-closing sub-elements
                    startElement(subElementName, false);
                    Map<String, String> subElementAttributesToEnforce = subElementToEnforce.getValue();
                    if (subElementAttributesToEnforce != null) {
                        for (Map.Entry<String, String> entry : subElementAttributesToEnforce.entrySet()) {
                            // since we're forcing this sub element, don't do the valid attribute check.
                            addAttribute(entry.getKey(), entry.getValue(), false);
                        }
                    }
                    super.endStartElement();
                    closeElement(subElementName, false);
                }
            }
        }
        attributesSupportedByCurrentElement = null;
        return ret;
    }

    protected int textNode(String text) {
        if (!currentAnchorHrefValue.isEmpty() && text.equalsIgnoreCase(currentAnchorHrefValue.peek())) {
            text = AnchorTextMassager.getTruncatedUrlString(text);
        } else if (options.autoLinkAnchorPattern!=null && currentAnchorHrefValue.isEmpty()) {
            text = options.autoLinkAnchorPattern.replace(text);
        }

        if (options.graemlinKeystrokeToReplacementHtml != null) {
            text = GraemlinMassager.doGraemlinsToHtml(text, options.graemlinKeystrokeToReplacementHtml, null);
        }

        return super.textNode(text);
    }

    protected int closeElement(String elementName, boolean isSelfClose) {
        assert !elements.empty() && IPStringUtil.isStringEqualIgnoreCase(elementName, elements.peek().getName()) : "Coding error.  Stack didn't match next close element! element/" + elementName + " isEmptyStack/" + elements.empty() + " topElementInStack/" + (elements.empty() ? "" : elements.peek().getName());
        ElementInfo elementInfo = elements.peek();
        // If the current element is to be excluded, escape from here.
        int ret;
        if (elementInfo.isExclude()) {
            ret = 0;
        } else {
            // close it if we had opened it
            escapeCurrentElement = elementInfo.isEscape();
            ret = super.closeElement(elementName, isSelfClose);
        }
        elements.pop();
        if ("a".equalsIgnoreCase(elementName)) {
            currentAnchorHrefValue.pop();
        }
        return ret;
    }

    /**
     * parse the given HTML document and strip out unsupported HTML elements and attributes.
     * all supported elements should have keys in the supplied Map.  the attributes supported
     * by a given element should be specified in a Collection in the value of the Map.
     *
     * @param html                                   the html from which to strip invalid/unsupported html elements/attributes
     * @param supportedElementsToSupportedAttributes a map containing the valid elements mapped to a collection of valid attributes.
     *                                               NOTE: all element and attribute names must be lower-case or else the lookups may fail!
     * @return the html with invalid/unsupported html stripped
     */
    public static String enforceHtmlElementsAndAttributes(String html, Options options, Map<String, Collection<String>> supportedElementsToSupportedAttributes, Map<String, Collection<NameValuePair<String>>> elementNamesToAttributeNameValueToEnforce, Map<String, Collection<NameValuePair<Map<String, String>>>> elementNamesToSubElementToEnforce, Map<String, Map<String, Collection<String>>> elementNamesToAttributesWithLimitedValues, HTMLParser.FragmentType fragmentType) {
        return new HTMLEnforcer(html, options, fragmentType, supportedElementsToSupportedAttributes, elementNamesToAttributeNameValueToEnforce, elementNamesToSubElementToEnforce, elementNamesToAttributesWithLimitedValues).parse();
    }

    public static String autoLinkUrlsWithoutEnforcingHtml(String html, AnchorPattern anchorPattern, Collection<? extends AnchorConditionalAttribute> conditionalAttributes) {
        Options options = new Options(null);
        options.setAllowAnyHtml(true);
        options.setAutoLinkAnchorPattern(anchorPattern);
        return new HTMLEnforcer(html, options, FragmentType.BODY).parse();
    }

    private static class ElementInfo {
        private final String name;
        private final boolean isEscape;
        private boolean isExclude;

        private ElementInfo(String name, boolean isEscape) {
            this.name = name;
            this.isEscape = isEscape;
        }

        public String getName() {
            return name;
        }

        public boolean isEscape() {
            return isEscape;
        }

        public boolean isExclude() {
            return isExclude;
        }

        public void setExclude(boolean exclude) {
            isExclude = exclude;
        }
    }

    public static class Options {
        private boolean isAllowAnyHtml = false;
        private AnchorPattern autoLinkAnchorPattern;
        private final Map<String, Graemlin> graemlinKeystrokeToReplacementHtml;

        public Options() {
            this(null);
        }

        public Options(Map<String, Graemlin> graemlinKeystrokeToReplacementHtml) {
            this.graemlinKeystrokeToReplacementHtml = graemlinKeystrokeToReplacementHtml;
        }

        public void setAllowAnyHtml(boolean allowAnyHtml) {
            isAllowAnyHtml = allowAnyHtml;
        }

        private void setAutoLinkAnchorPattern(AnchorPattern autoLinkAnchorPattern) {
            this.autoLinkAnchorPattern = autoLinkAnchorPattern;
        }
    }
}

