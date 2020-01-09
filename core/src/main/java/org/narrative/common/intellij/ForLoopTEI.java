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
public class ForLoopTEI extends TagExtraInfo {
    public VariableInfo[] getVariableInfo(TagData tagData) {

        String varStatus = (String) tagData.getAttribute("varStatus");

        return new VariableInfo[]{new VariableInfo(varStatus, "javax.servlet.jsp.jstl.core.LoopTagStatus", true, VariableInfo.NESTED)};
    }
}
