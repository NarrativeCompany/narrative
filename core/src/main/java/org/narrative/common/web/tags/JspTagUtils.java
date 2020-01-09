package org.narrative.common.web.tags;

import org.narrative.common.util.XMLUtil;
import org.narrative.common.util.posting.HtmlTextMassager;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Created by IntelliJ IDEA.
 * User: paul
 * Date: Jul 6, 2006
 * Time: 9:38:16 AM
 */
public class JspTagUtils {
    private static final Collection<String> LOWERCASE_BUTTON_TYPES = Collections.unmodifiableSet(newHashSet(Arrays.asList("button", "submit")));

    public static String createInput(String type, String name, String value, boolean enableDisabledHtmlInValue, String formId, String size, String maxLength, String accept, boolean disabled, boolean readonly, boolean isChecked, String tabIndex, String id, String cssClass, String cssStyle, String title, String onClick, String onDoubleClick, String onMouseDown, String onMouseUp, String onMouseOver, String onMouseMove, String onMouseOut, String onFocus, String onBlur, String onKeyPress, String onKeyDown, String onKeyUp, String onPaste, String onCut, String onInput, String onSelect, String onChange, String autocomplete, String body) {

        // bl: name and value are not required fields.
        //assert !isEmpty(name) : "Name is a required value";
        assert !isEmpty(type) : "Type is a required value";
        //assert !isEmpty(value) : "Value is a required value";

        id = createInputId(id, formId, name);

        if (enableDisabledHtmlInValue) {
            value = HtmlTextMassager.enableDisabledHtml(value);
        }

        boolean isButtonType = LOWERCASE_BUTTON_TYPES.contains(type.toLowerCase());
        assert isEmpty(body) || isButtonType : "Should only ever specify a body when specifying custom text for a button!";

        // jw: if we are trying to use a different value for the body of a button than the value we need to be sure to use a button element.
        String element = (!isEmpty(body) && isButtonType) ? "button" : "input";
        StringBuilder sb = new StringBuilder("<").append(element).append(attr("type", type)).append(attr("name", name)).append(type.toLowerCase().equals("file") ? "" : attr("value", value)).append(condAttr("size", size)).append(condAttr("maxlength", maxLength)).append(condAttr("accept", accept)).append(disabled ? attr("disabled", "disabled") : "").append(readonly ? attr("readonly", "readonly") : "").append(isChecked ? attr("checked", "checked") : "").append(condAttr("tabindex", tabIndex)).append(condAttr("accept", accept)).append(condAttr("id", id)).append(condAttr("class", cssClass)).append(condAttr("style", cssStyle)).append(condAttr("title", title)).append(condAttr("onclick", onClick)).append(condAttr("ondblclick", onDoubleClick)).append(condAttr("onmousedown", onMouseDown)).append(condAttr("onmouseup", onMouseUp)).append(condAttr("onmouseover", onMouseOver)).append(condAttr("onmousemove", onMouseMove)).append(condAttr("onmouseout", onMouseOut)).append(condAttr("onfocus", onFocus)).append(condAttr("onblur", onBlur)).append(condAttr("onkeypress", onKeyPress)).append(condAttr("onKeyDown", onKeyDown)).append(condAttr("onKeyUp", onKeyUp)).append(condAttr("onpaste", onPaste)).append(condAttr("oncut", onCut)).append(condAttr("oninput", onInput)).append(condAttr("onSelect", onSelect)).append(condAttr("onChange", onChange)).append(condAttr("autocomplete", autocomplete));

        if (isEmpty(body)) {
            sb.append("/>");
        } else {
            sb.append(">").append(body).append("</").append(element).append(">");
        }

        return sb.toString();
    }

    public static String createInputId(String id, String formId, String name) {
        if (!isEmpty(id)) {
            return id;
        }
        if (!isEmpty(formId)) {
            return formId + '_' + escapeNameForUseInJavascript(name);
        }
        return escapeNameForUseInJavascript(name);
    }

    /**
     * make a name easier to use in javascript by replacing all
     * occurrences of ., [, ], and ' ' with _
     *
     * @param name the name to escape
     * @return the escaped copy of the name
     */
    public static String escapeNameForUseInJavascript(String name) {
        return name.replaceAll("[\\'\\.\\[\\]\\ ]", "_");
    }

    public static String getHtmlAttributeString(Map<String, Object> attributeMap) {
        if (isEmptyOrNull(attributeMap)) {
            return null;
        }
        StringBuilder ret = new StringBuilder();
        for (Map.Entry<String, Object> entry : attributeMap.entrySet()) {
            String name = entry.getKey();
            Object objValue = entry.getValue();
            XMLUtil.addNameEqualsValueAttribute(ret, name, objValue == null ? null : objValue.toString(), true /* for html */);
        }
        return ret.toString();
    }

    public static Map<String, Object> getFoundationTooltipAttributes(String title, boolean includeClass) {
        if (isEmpty(title)) {
            return Collections.emptyMap();
        }
        Map<String, Object> ret = newLinkedHashMap();
        ret.put("data-tooltip", null);
        ret.put("aria-haspopup", "true");
        if (includeClass) {
            ret.put("class", "has-tooltip");
        }
        ret.put("title", title);
        return ret;
    }

    public static String getFoundationTooltipAttributeString(String title, boolean includeClass) {
        return getHtmlAttributeString(getFoundationTooltipAttributes(title, includeClass));
    }
}

