package org.narrative.common.util.tags;

/**
 * Created by IntelliJ IDEA.
 * User: paul
 * Date: Dec 23, 2005
 * Time: 11:32:34 AM
 * To change this template use File | Settings | File Templates.
 */
public class VarVarSetTag extends SetTag {
    public void setVar(String var) {
        super.setVar(var);
        setClassName(Object.class.getName());
    }
}
