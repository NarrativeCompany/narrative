package org.narrative.common.util.tags;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.tagext.TagSupport;

import java.io.IOException;

/**
 * Date: May 24, 2007
 * Time: 9:20:11 AM
 *
 * @author Brian
 */
public abstract class StringWriterTagSupport extends TagSupport {
    public abstract String getOutputString();

    public int doEndTag() throws JspException {
        try {
            pageContext.getOut().print(getOutputString());
        } catch (IOException e) {
            throw new JspTagException("Failed writing static img tag!", e);
        }

        return EVAL_PAGE;
    }
}
