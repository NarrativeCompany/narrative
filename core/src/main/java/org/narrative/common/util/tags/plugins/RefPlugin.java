package org.narrative.common.util.tags.plugins;

import org.apache.jasper.compiler.tagplugin.TagPlugin;
import org.apache.jasper.compiler.tagplugin.TagPluginContext;

/**
 * Created by IntelliJ IDEA.
 * User: Paul
 * Date: Nov 7, 2006
 * Time: 12:11:45 PM
 * To change this template use File | Settings | File Templates.
 */
public class RefPlugin implements TagPlugin {
    public void doTag(TagPluginContext ctxt) {
        // bl: this used to be a no-op, but now we will actually run some code to verify the ref tag is actually valid.
        // this is useful on development environments where an invalid ref may not otherwise be detected.

        boolean hasClassName = ctxt.isAttributeSpecified("className");
        String className = hasClassName ? ctxt.getConstantAttribute("className") : null;
        String classNameStr = (className == null || className.equals("")) ? "null" : "\"" + className + "\"";

        // bl: do this as an assert so that it can be bypassed at runtime when assertions are disabled
        ctxt.generateJavaSource("assert org.narrative.common.util.tags.RefTag.getRefObject(_jspx_page_context,");
        ctxt.generateAttribute("var");
        ctxt.generateJavaSource("," + classNameStr + ");");
    }
}
