package org.narrative.common.intellij;

import javax.servlet.jsp.tagext.TagData;
import javax.servlet.jsp.tagext.TagExtraInfo;
import javax.servlet.jsp.tagext.VariableInfo;

/**
 * Created by IntelliJ IDEA.
 * User: paul
 * Date: Dec 23, 2005
 * Time: 9:32:50 AM
 * To change this template use File | Settings | File Templates.
 */
public class VariableClassTEI extends TagExtraInfo {
    public VariableInfo[] getVariableInfo(TagData tagData) {
        String var = tagData.getAttributeString("var");
        String className = tagData.getAttributeString("className");
        // bl: don't use IPStringUtil here since it has a logger now and that logger will break auto-completion in IntelliJ.
        // basically, don't have any external dependencies in our TagExtraInfo classes.
        if (className == null || className.equals("")) {
            className = Object.class.getName();
        }
        return new VariableInfo[]{new VariableInfo(var, className, true, VariableInfo.AT_END)};
    }
}
