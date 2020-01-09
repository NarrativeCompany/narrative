package org.narrative.common.web.struts;

import org.narrative.common.util.CoreUtils;
import com.opensymphony.xwork2.conversion.impl.XWorkBasicConverter;

import java.lang.reflect.Constructor;
import java.lang.reflect.Member;
import java.sql.Timestamp;
import java.util.Map;

/**
 * Date: Jan 23, 2006
 * Time: 10:22:57 AM
 *
 * @author Brian
 */
public class DateConverter extends XWorkBasicConverter {

    public Object convertValue(Map context, Object o, Member member, String s, Object value, Class toType) {
        if (toType == String.class) {
            assert value instanceof Timestamp : "Value should be of type Timestamp if converting to String! value/" + value.getClass().getName();
            return value.toString();
        }
        String strVal;
        if (value instanceof String[]) {
            strVal = ((String[]) value)[0];
        } else {
            strVal = value.toString();
        }

        if (CoreUtils.isEmpty(strVal)) {
            return null;
        }

        // bl: if we can't parse the string value, then try parsing it as a long value next
        // nb. this should now support java.util.Date, java.sql.Date, and java.sql.Timestamp.
        try {
            long longVal = Long.valueOf(strVal);
            if (longVal > 0) {
                Constructor c = toType.getConstructor(Long.TYPE);
                return c.newInstance(longVal);
            }
        } catch (Throwable t) {
            // ignore
        }

        //bk: it would be nice to just pass the long values around
        try {
            return Timestamp.valueOf(strVal);
        } catch (Throwable t) {
            //ignore
        }

        //assert false : "We should always use the date picker or long values to set date like objects";
        return super.convertValue(context, o, member, s, value, toType);
    }
}