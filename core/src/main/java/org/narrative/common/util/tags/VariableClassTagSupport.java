package org.narrative.common.util.tags;

import org.narrative.common.util.IPStringUtil;
import org.narrative.common.util.IPUtil;
import org.narrative.common.util.UnexpectedError;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.BodyTagSupport;

/**
 * Created by IntelliJ IDEA.
 * User: paul
 * Date: Dec 23, 2005
 * Time: 10:17:21 AM
 */
public abstract class VariableClassTagSupport extends BodyTagSupport {
    private String var;
    private String className;
    protected int scope = PageContext.PAGE_SCOPE;
    private boolean setNullIfNotOfType;

    public abstract Object getVarObject();

    public void release() {
        super.release();
        var = null;
        className = null;
        scope = PageContext.PAGE_SCOPE;
        setNullIfNotOfType = false;
    }

    public int doEndTag() throws JspException {
        Object obj = getVarObject();
        setPageContextVariable(super.pageContext, var, obj, className, scope, setNullIfNotOfType);
        return EVAL_PAGE;
    }

    protected Class getResolvedClass() {
        return getResolvedClass(className, var);
    }

    protected static Class getResolvedClass(String className, String var) {
        try {
            return Class.forName(IPUtil.getBinaryClassNameFromFullyQualifiedClassName(className));
        } catch (ClassNotFoundException e) {
            throw UnexpectedError.getRuntimeException("Unable to resolve class name " + className + " for var: " + var, e);
        }
    }

    public static void setPageContextVariable(PageContext pageContext, String var, Object obj, String className, int scope, boolean setNullIfNotOfType) {
        // make sure that this object is assignable from the specified class
        if (obj != null && !IPStringUtil.isEmpty(className)) {
            if (!getResolvedClass(className, var).isAssignableFrom(obj.getClass())) {
                if (setNullIfNotOfType) {
                    // not of the proper type?  then set the object to null since that is what they asked for.
                    // this effectively allows us to do type checks in the JSP.
                    obj = null;
                } else {
                    throw UnexpectedError.getRuntimeException("Class mismatch in ss:set tag.  Specified " + className + " (resolved to: " + IPUtil.getBinaryClassNameFromFullyQualifiedClassName(className) + ") but actual class was " + obj.getClass().getName() + " for variable " + var, true);
                }
            }
        }
        //get the object
        pageContext.setAttribute(var, obj, scope);
    }

    public String getVar() {
        return var;
    }

    public void setVar(String var) {
        this.var = var;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public boolean isSetNullIfNotOfType() {
        return setNullIfNotOfType;
    }

    public void setSetNullIfNotOfType(boolean setNullIfNotOfType) {
        this.setNullIfNotOfType = setNullIfNotOfType;
    }

    public void setScope(String scope) {
        this.scope = org.apache.taglibs.standard.tag.common.core.Util.getScope(scope);
    }
}
