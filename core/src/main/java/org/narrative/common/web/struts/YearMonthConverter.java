package org.narrative.common.web.struts;

import java.time.YearMonth;
import java.time.format.DateTimeParseException;

/**
 * Date: 9/20/19
 * Time: 11:22 AM
 *
 * @author brian
 */
public class YearMonthConverter extends StrutsStringConverterBase {
    @Override
    protected Class getConvertedClass() {
        return YearMonth.class;
    }

    @Override
    protected Object getConvertedObjectFromString(String val) {
        try {
            return YearMonth.parse(val);
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}
