package org.narrative.common.web.tags;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyTagSupport;

/**
 * Date: Mar 6, 2006
 * Time: 3:14:44 PM
 *
 * @author Brian
 */
public class URLParam extends BodyTagSupport {
    private String name;
    private String value;
    private boolean isValueSet;

    public void release() {
        super.release();
        name = null;
        value = null;
        isValueSet = false;
    }

    public int doEndTag() throws JspException {
        URLTag urlTag = (URLTag) findAncestorWithClass(this, URLTag.class);
        if (urlTag == null) {
            throw new JspException("URLParam tags must be nested inside of a URLTag!");
        }

        String valueToSet;
        if (isValueSet) {
            valueToSet = value;
        } else {
            if (bodyContent == null || bodyContent.getString() == null) {
                valueToSet = "";
            } else {
                valueToSet = bodyContent.getString().trim();
            }
        }

        urlTag.addParameter(name, valueToSet);

        return EVAL_PAGE;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setValue(String value) {
        this.value = value;
        this.isValueSet = true;
    }
}
