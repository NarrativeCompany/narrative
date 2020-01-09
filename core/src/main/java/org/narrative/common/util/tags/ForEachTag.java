package org.narrative.common.util.tags;

import org.narrative.common.util.IPUtil;
import org.narrative.common.util.UnexpectedError;

import javax.servlet.jsp.JspTagException;

/**
 * Created by IntelliJ IDEA.
 * User: paul
 * Date: Mar 21, 2006
 * Time: 2:35:47 PM
 */
public class ForEachTag extends org.apache.taglibs.standard.tag.rt.core.ForEachTag {

    private String obj;
    private String className;

    // Releases any resources we may have (or inherit)
    public void release() {
        super.release();
        obj = null;
        className = null;
    }

    protected Object next() throws JspTagException {
        Object ret = super.next();
        ensureForEachObjectOfProperClass(ret, className, obj);
        return ret;
    }

    protected Class getResolvedClass() {
        return getResolvedClass(className, obj);
    }

    private static Class getResolvedClass(String className, String var) {
        try {
            return Class.forName(IPUtil.getBinaryClassNameFromFullyQualifiedClassName(className));
        } catch (ClassNotFoundException e) {
            throw UnexpectedError.getRuntimeException("Unable to resolve class name " + className + " for forEach var: " + var, e);
        }
    }

    public static void ensureForEachObjectOfProperClass(Object obj, String className, String var) {
        // make sure the object being returned is of the proper type.  if it is not, return a more useful error than
        // the class cast being returned currently.
        if (obj != null && !getResolvedClass(className, var).isAssignableFrom(obj.getClass())) {
            throw UnexpectedError.getRuntimeException("Class mismatch in ss:forEach tag.  Specified " + className + " (resolved to: " + IPUtil.getBinaryClassNameFromFullyQualifiedClassName(className) + ") but actual class was " + obj.getClass().getName() + " for variable " + var, true);
        }
    }

    public void setObj(String obj) {
        super.setVar(obj);
        this.obj = obj;
    }

    public void setClassName(String className) {
        this.className = className;
    }
}

