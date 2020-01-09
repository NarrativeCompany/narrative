package org.narrative.common.util.html;

/**
 * Date: 2019-04-22
 * Time: 09:34
 *
 * @author brian
 */
public class HTMLAttributeStripper extends HTMLEnforcer {
    private final String attributeName;

    public HTMLAttributeStripper(String attributeName, String html, FragmentType fragmentType) {
        super(html, createOptions(), fragmentType, null, null, null, null);
        this.attributeName = attributeName.toLowerCase();
    }

    @Override
    protected boolean isElementValid(String elementNameLowercase) {
        // bl: all elements are valid. we're just stripping a single attribute
        return true;
    }

    @Override
    protected boolean isAttributeSupportedByElement(String elementNameLowercase, String attributeNameLowercase) {
        // bl: support all attributes other than the attribute we are stripping
        return !attributeNameLowercase.equals(attributeName);
    }

    private static Options createOptions() {
        Options options = new Options();
        options.setAllowAnyHtml(true);
        return options;
    }
}
