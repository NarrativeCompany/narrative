package org.narrative.common.web.struts;

import org.narrative.common.util.IPUtil;
import ognl.DefaultTypeConverter;

import java.util.Locale;
import java.util.Map;

/**
 * Date: Jan 23, 2006
 * Time: 10:22:57 AM
 *
 * @author Brian
 */
public class LocaleConverter extends DefaultTypeConverter {
    public Object convertValue(Map context, Object value, Class toType) {
        if (toType == String.class) {
            return ((Locale) value).toString();
        }
        if (toType == Locale.class) {
            String strVal;
            if (value instanceof String[]) {
                strVal = ((String[]) value)[0];
            } else {
                strVal = value.toString();
            }
            return IPUtil.parseLocaleString(strVal);
        }
        return null;
    }
}
