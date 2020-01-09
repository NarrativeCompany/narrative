package org.narrative.common.util.tags;

import org.narrative.common.util.IPUtil;
import org.narrative.common.util.UnexpectedError;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

/**
 * Created by IntelliJ IDEA.
 * User: paul
 * Date: Jan 4, 2006
 * Time: 9:14:24 AM
 * This tag exists simply to create typed variable references that can be recognized by intellij
 */
public class RefTag extends VariableClassTagSupport {

    public Object getVarObject() {
        throw UnexpectedError.getRuntimeException("This method shouldn't ever be called!");
    }

    public int doEndTag() throws JspException {
        // bl: call this so that we'll resolve any errors if we can't identify the object or the class doesn't match.
        // bl: note that this probably never gets used in practice since we have the RefPlugin that calls getRefObject directly.
        assert getRefObject(pageContext, getVar(), getClassName()) : "Failed ref validation!";
        return EVAL_PAGE;
    }

    public static boolean getRefObject(PageContext pageContext, String var, String className) {
        // bl: most refs will be in request scope
        Object attr = pageContext.getAttribute(var, PageContext.REQUEST_SCOPE);
        if (attr == null) {
            // if not found in the request, try page scope. don't support anything else currently.
            attr = pageContext.getAttribute(var, PageContext.PAGE_SCOPE);
        }
        if (attr != null) {
            assert getResolvedClass(className, var).isAssignableFrom(attr.getClass()) : "Class mismatch in ss:ref tag.  Specified " + className + " (resolved to: " + IPUtil.getBinaryClassNameFromFullyQualifiedClassName(className) + ") but actual class was " + attr.getClass().getName() + " for variable " + var;
        }/* else {
            // bl: it's perfectly valid to do a ref of an object that may not exist.
            throw UnexpectedError.getRuntimeException("Should not reference an object that doesn't exist in the pageContext.", true);
        }*/

        // bl: just always return true since this method should only ever be called in an assert for validation purposes
        return true;
    }
}
