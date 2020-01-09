package org.narrative.common.web.struts;

import org.narrative.common.util.IPUtil;
import com.opensymphony.xwork2.conversion.impl.XWorkBasicConverter;

import java.lang.reflect.Member;
import java.util.Map;

/**
 * Date: Jan 17, 2008
 * Time: 2:03:42 PM
 *
 * @author brian
 */
public class BooleanConverter extends XWorkBasicConverter {

    public Object convertValue(Map context, Object o, Member member, String s, Object value, Class toType) {
        if (toType == String.class) {
            return ((Boolean) value).toString();
        }
        if (toType == Boolean.class) {
            String strVal;
            if (value instanceof String[]) {
                strVal = ((String[]) value)[0];
            } else {
                strVal = value.toString();
            }
            // bl: for legacy support, also support converting a "Y" or "y" to true.
            return IPUtil.getBooleanObjectFromString(strVal);
        }
        return super.convertValue(context, o, member, s, value, toType);
    }
}
