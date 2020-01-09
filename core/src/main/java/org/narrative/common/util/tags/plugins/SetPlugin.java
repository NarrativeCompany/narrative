package org.narrative.common.util.tags.plugins;

import org.apache.jasper.compiler.tagplugin.TagPlugin;
import org.apache.jasper.compiler.tagplugin.TagPluginContext;
import org.apache.jasper.tagplugins.jstl.Util;

/**
 * Created by IntelliJ IDEA.
 * User: Paul
 * Date: Nov 7, 2006
 * Time: 12:13:06 PM
 * To change this template use File | Settings | File Templates.
 */
public class SetPlugin implements TagPlugin {
    public void doTag(TagPluginContext ctxt) {

        //the flags to indicate whether the attributes have been specified
        //initialize the flags
        boolean hasValue = ctxt.isAttributeSpecified("object");
        if (!hasValue) {
            ctxt.dontUseTagPlugin();
            return;
        }

        boolean hasScope = ctxt.isAttributeSpecified("scope");
        int scope = Util.getScope(hasScope ? ctxt.getConstantAttribute("scope") : "page");

        boolean hasNullIfNotType = ctxt.isAttributeSpecified("setNullIfNotOfType");
        boolean setNullIfNotOfType = hasNullIfNotType && ctxt.getConstantAttribute("setNullIfNotOfType").equals("true");

        boolean hasClassName = ctxt.isAttributeSpecified("className");
        String className = hasClassName ? ctxt.getConstantAttribute("className") : null;
        String classNameStr = (className == null || className.equals("")) ? "null" : "\"" + className + "\"";

        ctxt.generateJavaSource("org.narrative.common.util.tags.VariableClassTagSupport.setPageContextVariable(_jspx_page_context,");
        ctxt.generateAttribute("var");
        ctxt.generateJavaSource(",");
        ctxt.generateAttribute("object");
        ctxt.generateJavaSource("," + classNameStr + ", " + scope + ", " + setNullIfNotOfType + ");");
    }
}
