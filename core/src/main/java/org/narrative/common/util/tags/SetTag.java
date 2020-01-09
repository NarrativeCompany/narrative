package org.narrative.common.util.tags;

/**
 * Created by IntelliJ IDEA.
 * User: paul
 * Date: Dec 23, 2005
 * Time: 11:32:34 AM
 */
public class SetTag extends VariableClassTagSupport {
    private Object object;
    private boolean isObjectSet;
    private boolean isDontTrim;

    public void release() {
        super.release();
        object = null;
        isObjectSet = false;
        isDontTrim = false;
    }

    public Object getVarObject() {
        // bl: if the object was set, then ignore the body content.
        if (isObjectSet) {
            // bl: changing to just return the object.  shouldn't ever need functionality
            // to look up the object in the pageContext (just use the expression language for that!)
            return object;
            
            /*if(object==null)
                return null;
            
            if (object instanceof String && !asString) {
                //if its a string, look it up int the pageContext
                return pageContext.getAttribute((String) object);
            }
            
            assert !asString : "Shouldn't set asString to true when the object you are setting isn't actually a String!  Could lead to unexpected behavior!";
            
            //otherwise just return the object itself.
            return object;*/
        }

        assert getClassName() == null || String.class.getName().equals(getClassName()) : "If using bodyContent, then the className must be either unspecified or java.lang.String!";
        // no object specified?  then let's use the result of evaluating the body
        if (bodyContent == null || bodyContent.getString() == null) {
            return "";
        }
        if (isDontTrim) {
            return bodyContent.getString();
        }
        return bodyContent.getString().trim();
    }

    public Object getObject() {
        return object;
    }

    public void setObject(Object object) {
        this.object = object;
        this.isObjectSet = true;
    }

    public void setDontTrim(boolean isDontTrim) {
        this.isDontTrim = isDontTrim;
    }
}