package org.narrative.common.web.struts.tags;

import org.narrative.common.util.tags.VariableClassTagSupport;
import com.opensymphony.xwork2.ActionContext;

/**
 * Created by IntelliJ IDEA.
 * User: paul
 * Date: Dec 23, 2005
 * Time: 9:23:58 AM
 * To change this template use File | Settings | File Templates.
 */
public class SetTag extends VariableClassTagSupport {
    private String ognl;

    public void release() {
        super.release();
        ognl = null;
    }

    public Object getVarObject() {
        return ActionContext.getContext().getValueStack().findValue(ognl);
    }

    public String getOgnl() {
        return ognl;
    }

    public void setOgnl(String ognl) {
        this.ognl = ognl;
    }
}
