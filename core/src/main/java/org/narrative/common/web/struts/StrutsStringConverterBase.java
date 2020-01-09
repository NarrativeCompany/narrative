package org.narrative.common.web.struts;

import ognl.DefaultTypeConverter;

import java.util.Map;

/**
 * Date: Mar 23, 2009
 * Time: 11:18:58 AM
 *
 * @author brian
 */
public abstract class StrutsStringConverterBase extends DefaultTypeConverter {
    public Object convertValue(Map map, Object object, Class toType) {
        if (toType == String.class) {
            return object.toString();
        }

        if (getConvertedClass().isAssignableFrom(toType)) {
            String strVal;
            if (object instanceof String[]) {
                strVal = ((String[]) object)[0];
            } else {
                strVal = object.toString();
            }
            return getConvertedObjectFromString(strVal);
        }
        return null;
    }

    protected abstract Class getConvertedClass();

    protected abstract Object getConvertedObjectFromString(String val);
}
