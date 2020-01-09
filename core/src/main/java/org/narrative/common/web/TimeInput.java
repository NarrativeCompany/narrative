package org.narrative.common.web;

import org.narrative.network.shared.util.NetworkCoreUtils;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Date: Aug 31, 2006
 * Time: 7:14:03 AM
 *
 * @author Brian
 */
public class TimeInput implements Validatable {
    /**
     * get a Calendar for the current day (date should be ignored).
     * nb. not lenient since the UI should enforce that valid values are supplied.
     * in other words, amPm should never be supplied when hourOfDay is supplied.
     */
    private Calendar cal = Calendar.getInstance(NetworkCoreUtils.networkContext().getFormatPreferences().getTimeZone());

    public static final String FROM_STRING_PARAM = "fromString";

    /**
     * bl: lame, I know.  keep track of which fields are set, so we know when to return -1
     * and when to return the time from the Calendar.
     */
    private boolean isHourSet = false;
    private boolean isHourOfDaySet = false;
    private boolean isMinuteSet = false;
    private boolean isAmPmSet = false;

    /**
     * @deprecated for Struts use only
     */
    public TimeInput() {}

    public TimeInput(Date date) {
        if (date != null) {
            cal.setTime(date);
            isHourSet = true;
            isHourOfDaySet = true;
            isMinuteSet = true;
            isAmPmSet = true;
        }
    }

    public TimeInput(Calendar cal) {
        this(cal, true);
    }

    /**
     * package-level access so it can only be used by DateAndTime.
     * we only want the DateAndTime object to be able to share
     * the internal Calendar used by TimeOfDay.
     *
     * @param cal
     * @param isInitialized
     */
    TimeInput(Calendar cal, boolean isInitialized) {
        if (cal != null) {
            this.cal = cal;
            if (isInitialized) {
                isHourSet = true;
                isHourOfDaySet = true;
                isMinuteSet = true;
                isAmPmSet = true;
            }
        }
    }

    public void setup(long millis) {
        this.cal.setTimeInMillis(millis);
        isHourSet = true;
        isHourOfDaySet = true;
        isMinuteSet = true;
        isAmPmSet = true;
    }

    public int getHour() {
        if (!isHourSet) {
            return -1;
        }
        return cal.get(Calendar.HOUR);
    }

    public void setHour(int hour) {
        if (hour < 0 || hour > 11) {
            isHourSet = false;
        } else {
            cal.set(Calendar.HOUR, hour);
            isHourSet = true;
        }
    }

    public int getHourOfDay() {
        if (!isHourOfDaySet) {
            return -1;
        }
        return cal.get(Calendar.HOUR_OF_DAY);
    }

    public void setHourOfDay(int hourOfDay) {
        if (hourOfDay < 0 || hourOfDay > 23) {
            isHourOfDaySet = false;
        } else {
            cal.set(Calendar.HOUR_OF_DAY, hourOfDay);
            isHourOfDaySet = true;
        }
    }

    public int getResolvedHourOfDay() {
        return cal.get(Calendar.HOUR_OF_DAY);
    }

    public int getMinute() {
        if (!isMinuteSet) {
            return -1;
        }
        return cal.get(Calendar.MINUTE);
    }

    public void setMinute(int minute) {
        if (minute < 0 || minute > 59) {
            isMinuteSet = false;
        } else {
            cal.set(Calendar.MINUTE, minute);

            //also set everything below a second to zero
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);

            isMinuteSet = true;
        }
    }

    public int getAmPm() {
        if (!isAmPmSet) {
            return -1;
        }
        return cal.get(Calendar.AM_PM);
    }

    public void setAmPm(int amPm) {
        if (amPm != Calendar.AM && amPm != Calendar.PM) {
            isAmPmSet = false;
        } else {
            cal.set(Calendar.AM_PM, amPm);
            isAmPmSet = true;
        }
    }

    public void setFromString(String value) {
        if (isEmpty(value)) {
            return;
        }
        String[] values = value.split(":");
        if (values.length != 2) {
            return;
        }

        int hour;
        int minute;
        try {
            hour = Integer.parseInt(values[0]);
            minute = Integer.parseInt(values[1]);
        } catch (NumberFormatException ignore) {
            return;
        }

        setHourOfDay(hour);
        setMinute(minute);
    }

    public String getFromString() {
        if (!isValid()) {
            return null;
        }

        // jw: we need to use the 0-23 format for the "fromString" to make it consistent with what was submitted in.
        return cal.get(Calendar.HOUR_OF_DAY) + ":" + getMinute();
    }

    public boolean isValid() {
        // for 24-hour, just need hour of day and minutes
        if (isHourOfDaySet) {
            return isMinuteSet;
        }
        // for 12-hour, need minutes and amPm
        if (isHourSet) {
            return isMinuteSet && isAmPmSet;
        }
        // neither hour nor hourOfDay is set?  then not valid.
        return false;
    }

    public void addParameter(String prefix, Map<String, Object> parameters) {
        if (isValid()) {
            parameters.put(prefix + "." + FROM_STRING_PARAM, getFromString());
        }
    }

}
