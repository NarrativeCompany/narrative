package org.narrative.common.intellij;

import javax.servlet.jsp.tagext.TagData;
import javax.servlet.jsp.tagext.TagExtraInfo;
import javax.servlet.jsp.tagext.VariableInfo;

/**
 * Created by IntelliJ IDEA.
 * User: paul
 * Date: Mar 21, 2006
 * Time: 2:36:00 PM
 * To change this template use File | Settings | File Templates.
 */
public class ForEachTEI extends TagExtraInfo {
    public VariableInfo[] getVariableInfo(TagData tagData) {
        String var = (String) tagData.getAttribute("obj");
        String className = (String) tagData.getAttribute("className");

        String varStatus = (String) tagData.getAttribute("varStatus");
        VariableInfo[] varInfo;
        // bl: don't use IPStringUtil here since it has a logger now and that logger will break auto-completion in IntelliJ.
        // basically, don't have any external dependencies in our TagExtraInfo classes.
        if (varStatus != null && !varStatus.equals("")) {
            varInfo = new VariableInfo[2];
            varInfo[1] = new VariableInfo(varStatus, "javax.servlet.jsp.jstl.core.LoopTagStatus", true, VariableInfo.NESTED);
        } else {
            varInfo = new VariableInfo[1];
        }
        varInfo[0] = new VariableInfo(var, className, true, VariableInfo.NESTED);

        return varInfo;
    }
}