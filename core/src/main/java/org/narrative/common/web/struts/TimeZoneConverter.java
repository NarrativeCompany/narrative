package org.narrative.common.web.struts;

import ognl.DefaultTypeConverter;

import java.util.Map;
import java.util.TimeZone;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Date: Jan 23, 2006
 * Time: 10:22:57 AM
 *
 * @author Brian
 */
public class TimeZoneConverter extends DefaultTypeConverter {
    public Object convertValue(Map context, Object value, Class toType) {
        if (toType == String.class) {
            return ((TimeZone) value).getID();
        }
        if (toType == TimeZone.class) {
            String strVal;
            if (value instanceof String[]) {
                strVal = ((String[]) value)[0];
            } else {
                strVal = value.toString();
            }
            if (!isEmpty(strVal)) {
                TimeZone timeZone = TimeZone.getTimeZone(strVal);
                // bl: only return the TimeZone if its ID actually matches the supplied value.
                // this is needed since getTimeZone will return GMT by default if the ID is unrecognized.
                // we don't want that behavior and instead want to ignore unknown values.
                if (isEqual(timeZone.getID(), strVal)) {
                    return timeZone;
                }
            }
        }
        return null;
    }
}
