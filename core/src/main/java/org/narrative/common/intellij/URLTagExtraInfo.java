package org.narrative.common.intellij;

import javax.servlet.jsp.tagext.TagData;
import javax.servlet.jsp.tagext.TagExtraInfo;
import javax.servlet.jsp.tagext.VariableInfo;

/**
 * Date: Mar 3, 2006
 * Time: 8:41:41 AM
 *
 * @author Brian
 */
public class URLTagExtraInfo extends TagExtraInfo {
    public VariableInfo[] getVariableInfo(TagData tagData) {
        String id = tagData.getAttributeString("id");
        // bl: don't use IPStringUtil here since it has a logger now and that logger will break auto-completion in IntelliJ.
        // basically, don't have any external dependencies in our TagExtraInfo classes.
        if (id == null || id.equals("")) {
            return null;
        }
        return new VariableInfo[]{new VariableInfo(id, String.class.getName(), true, VariableInfo.AT_END)};
    }
}
