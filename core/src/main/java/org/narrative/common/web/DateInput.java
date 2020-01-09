package org.narrative.common.web;

import org.narrative.common.util.CoreUtils;
import org.narrative.common.util.UnexpectedError;

import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;

import static org.narrative.network.shared.util.NetworkCoreUtils.*;

/**
 * User: barry
 * Date: Jul 10, 2009
 * Time: 1:53:40 PM
 */
public class DateInput {
    final Calendar cal;

    private boolean isDateSet = false;

    public static final String DATE_STRING_PARAM_SUFFIX = ".dateString";

    public DateInput() {
        this(networkContext().getFormatPreferences().getCalendar(), false);
    }

    public DateInput(TimeZone timeZone) {
        this(timeZone != null ? Calendar.getInstance(timeZone) : networkContext().getFormatPreferences().getCalendar(), false);
    }

    public DateInput(Date date) {
        this();
        if (date != null) {
            cal.setTime(date);
            isDateSet = true;
        }
    }

    public DateInput(Date date, TimeZone timeZone) {
        this(timeZone);
        if (date != null) {
            cal.setTime(date);
            isDateSet = true;
        }
    }

    public DateInput(Calendar cal) {
        this(cal, true);
    }

    private DateInput(Calendar cal, boolean initialized) {
        if (cal == null) {
            throw UnexpectedError.getRuntimeException("Should never attempt to create a DateInput with the Calendar constructor without providing a actual calendar");
        }

        this.cal = cal;
        isDateSet = initialized;
    }

    public void setup(long millis) {
        this.cal.setTimeInMillis(millis);
        isDateSet = true;
    }

    //Date Format is MM/DD/YYYY
    public void setDateString(String dateString) throws NumberFormatException {
        // bl: set it to false by default. if there is a date set below, isDateSet will be set to true.
        isDateSet = false;
        if (!CoreUtils.isEmpty(dateString)) {
            String[] parsedDate = dateString.split("/");
            if (parsedDate.length == 3) {
                cal.set(Integer.parseInt(parsedDate[2]), Integer.parseInt(parsedDate[0]) - 1, Integer.parseInt(parsedDate[1]));
                isDateSet = true;
            }
        }
    }

    //Date Format is MM/DD/YYYY
    public String getDateString() {
        if (!isDateSet) {
            return null;
        }

        StringBuilder sb = new StringBuilder(10);
        sb.append(cal.get(Calendar.MONTH) + 1).append("/");
        sb.append(cal.get(Calendar.DAY_OF_MONTH)).append("/");
        sb.append(cal.get(Calendar.YEAR));
        return sb.toString();
    }

    public LocalDate getLocalDate() {
        if (isValid()) {
            return LocalDate.of(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DATE));
        }
        return null;
    }

    public Calendar getCalendar() {
        if (!isValid()) {
            return null;
        }
        return cal;
    }

    public boolean isValid() {
        return isDateSet;
    }

    public void setTimeZone(TimeZone timeZone) {
        cal.setTimeZone(timeZone);
    }

    public void addParameter(String prefix, Map<String, Object> parameters) {
        if (isDateSet) {
            parameters.put(prefix + DATE_STRING_PARAM_SUFFIX, getDateString());
        }
    }

}
