package org.narrative.common.util;

import org.apache.commons.lang3.time.DateUtils;
import org.apache.commons.lang3.time.FastDateFormat;

import java.sql.Date;
import java.sql.Timestamp;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

public class IPDateUtil {
    public static final int SECOND_IN_MS = 1000;
    public static final long LONG_SECOND_IN_MS = SECOND_IN_MS;
    public static final int MINUTE_IN_SECONDS = 60;
    public static final int MINUTE_IN_MS = MINUTE_IN_SECONDS * SECOND_IN_MS;
    public static final int HOUR_IN_MINUTES = 60;
    public static final int HOUR_IN_SECONDS = HOUR_IN_MINUTES * MINUTE_IN_SECONDS;
    public static final int HOUR_IN_MS = HOUR_IN_SECONDS * SECOND_IN_MS;
    public static final int DAY_IN_HOURS = 24;
    public static final int DAY_IN_MINUTES = DAY_IN_HOURS * HOUR_IN_MINUTES;
    public static final int DAY_IN_SECONDS = DAY_IN_MINUTES * MINUTE_IN_SECONDS;
    public static final long DAY_IN_MS = DAY_IN_SECONDS * SECOND_IN_MS;
    public static final int WEEK_IN_DAYS = 7;
    public static final long WEEK_IN_MS = DAY_IN_MS * WEEK_IN_DAYS;
    public static final int YEAR_IN_DAYS = 365;
    public static final int YEAR_IN_HOURS = YEAR_IN_DAYS * DAY_IN_HOURS;
    public static final int YEAR_IN_MINUTES = YEAR_IN_HOURS * HOUR_IN_MINUTES;
    public static final int YEAR_IN_SECONDS = YEAR_IN_MINUTES * MINUTE_IN_SECONDS;
    public static final long YEAR_IN_MS = DAY_IN_MS * YEAR_IN_DAYS;

    public static final TimeZone UTC_TIMEZONE = TimeZone.getTimeZone("UTC");

    private static final NarrativeLogger logger = new NarrativeLogger(IPDateUtil.class);

    /**
     * a utility to convert a Date to a sql date.
     * handles null dates and dates that are already sql
     * dates
     */
    private static java.sql.Date getSQLDateFromDate(java.util.Date date) {
        if (date == null) {
            return null;
        }
        if (date instanceof java.sql.Date) {
            return (java.sql.Date) date;
        }
        return new java.sql.Date(date.getTime());
    }

    static public java.util.Date getStartOfDayFromDate(java.util.Date d) {
        return DateUtils.truncate(d, Calendar.DAY_OF_MONTH);
    }

    public static Calendar getCalendarTruncatedToDate(Calendar calendar) {
        return DateUtils.truncate(calendar, Calendar.DATE);
    }

    public static Calendar getCalendarTruncatedToMinute(Calendar calendar) {
        return DateUtils.truncate(calendar, Calendar.MINUTE);
    }

    public static Calendar getCalendarAtEndOFDay(Calendar calendar) {
        Calendar result = (Calendar) calendar.clone();
        result.set(Calendar.HOUR_OF_DAY, 23);
        result.set(Calendar.MINUTE, 59);
        result.set(Calendar.SECOND, 59);
        result.set(Calendar.MILLISECOND, 999);
        return result;
    }

    public static java.sql.Date getEndOfDayFromDate(java.util.Date d) {
        java.util.Date startOfDay = DateUtils.truncate(d, Calendar.DAY_OF_MONTH);
        return new java.sql.Date(startOfDay.getTime() + DAY_IN_MS - 1);
    }

    public static java.util.Date getEndOfDay(Calendar cal) {
        cal = DateUtils.truncate(cal, Calendar.DATE);
        return new java.util.Date(cal.getTimeInMillis() + DAY_IN_MS - 1);
    }

    public static Calendar getEndOfDayCal(Calendar cal) {
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);
        return cal;
    }

    /**
     * a utility to convert a Date to a sql timestamp.
     * handles null dates and dates that are already sql
     * dates
     */
    public static java.sql.Timestamp getTimestampFromDate(java.util.Date date) {
        if (date == null) {
            return null;
        }
        if (date instanceof java.sql.Timestamp) {
            return (java.sql.Timestamp) date;
        }
        return new java.sql.Timestamp(date.getTime());
    }

    /**
     * Returns a the current time timestamp less the specified amount of time
     *
     * @param ms
     * @return
     */
    public static Timestamp getTimestampLess(long ms) {
        return new Timestamp(System.currentTimeMillis() - ms);
    }

    /**
     * a utility to convert a date of birth to the corresponding age (in years).
     *
     * @param dob the date of birth to get the current age for
     * @return the age of a person right now based on the date of birth
     */
    public static int getAgeFromDOB(java.util.Date dob) {
        return getAgeFromDOB(dob, new Date(System.currentTimeMillis()));
    }

    /**
     * a utility to convert a date of birth to the corresponding age (in years).
     *
     * @param dob       the date of birth to get the current age for
     * @param ageAtDate the date at which to calculate the person's age
     * @return the age of a person right now based on the date of birth
     */
    public static int getAgeFromDOB(java.util.Date dob, java.util.Date ageAtDate) {
        return (int) Math.floor(((double) ageAtDate.getTime() - dob.getTime()) / (double) YEAR_IN_MS);
    }

    public static Calendar getStartOfMonth(Calendar cal) {
        Calendar gc = (Calendar) cal.clone();
        gc.set(Calendar.DAY_OF_MONTH, 1);
        gc.set(Calendar.HOUR_OF_DAY, 0);
        gc.set(Calendar.MINUTE, 0);
        gc.set(Calendar.SECOND, 0);
        gc.set(Calendar.MILLISECOND, 0);
        return gc;
    }

    public static Calendar getStartOfPreviousMonth(Calendar cal) {
        Calendar endOfPreviousMonth = getEndOfPreviousMonth(cal);
        return getStartOfMonth(endOfPreviousMonth);
    }

    public static Calendar getEndOfMonth(Calendar cal) {
        // jw: just like the rest of the methods here, we need to make sure we clone the calendar, because modifying the calendar that was passed in is entirely unexpected, and
        //     ultimately causes more problems than it solves. If this method is meant to set a calendar to the end of the month, than dont have a return value. Returning a calendar
        //     makes the contract appear to not modify the passed in cal!
        cal = (Calendar) cal.clone();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.add(Calendar.MONTH, 1);
        cal.add(Calendar.DAY_OF_MONTH, -1);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);
        if (logger.isDebugEnabled()) {
            logger.debug("End of month for " + cal);
        }
        return cal;
    }

    public static Calendar getEndOfPreviousMonth(Calendar cal) {
        Calendar gc = (Calendar) cal.clone();
        gc.set(Calendar.DAY_OF_MONTH, 1);
        gc.add(Calendar.DAY_OF_MONTH, -1);
        gc.set(Calendar.HOUR_OF_DAY, 23);
        gc.set(Calendar.MINUTE, 59);
        gc.set(Calendar.SECOND, 59);
        gc.set(Calendar.MILLISECOND, 999);
        return gc;
    }

    /**
     * figure out if a date is before another day, disregarding the time of day
     *
     * @param date1 the date to check
     * @param date2 the date to compare against
     * @return true if date1 is earlier than date2 in terms of date, disregarding time of day
     */
    public static boolean isDayBeforeDay(java.util.Date date1, java.util.Date date2) {
        if (date1 == null || date2 == null) {
            return false;
        }
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal1.setTime(date1);
        cal2.setTime(date2);
        return isDayBeforeDay(cal1, cal2);
    }

    /**
     * figure out if a date is before another day, disregarding the time of day
     *
     * @param cal1 the date to check
     * @param cal2 the date to compare against
     * @return true if cal1 is earlier than cal2 in terms of date, disregarding time of day
     */
    public static boolean isDayBeforeDay(Calendar cal1, Calendar cal2) {
        if (cal2.get(Calendar.ERA) > cal1.get(Calendar.ERA)) {
            return true;
        } else if (cal2.get(Calendar.ERA) < cal1.get(Calendar.ERA)) {
            return false;
        } else if (cal2.get(Calendar.YEAR) > cal1.get(Calendar.YEAR)) { //eras equal
            return true;
        } else if (cal2.get(Calendar.YEAR) < cal1.get(Calendar.YEAR)) { //eras equal
            return false;
        } else if (cal2.get(Calendar.DAY_OF_YEAR) > cal1.get(Calendar.DAY_OF_YEAR)) { //eras and years equal
            return true;
        }

        //eras and years equal, cal1's day of year > or equal to cal2's day of year
        return false;
    }

    public static int getYearFromDate(java.util.Date date) {
        if (date == null) {
            return 0;
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal.get(Calendar.YEAR);
    }

    /**
     * a utility to convert a Date in the format yyyy-MM-ddto a sql date.
     * handles null dates and dates that are already sql
     * dates
     */
    public static String getYYYYMMDDStringFromCalendar(Calendar cal) {
        if (cal == null) {
            return "";
        }

        return s_yyyymmddDateFormat.format(cal);
    }

    public static String getYYYYMMDDStringFromDate(java.util.Date date) {
        if (date == null) {
            return "";
        }

        return s_yyyymmddDateFormat.format(date);
    }

    private static final FastDateFormat s_yyyymmddDateFormat = FastDateFormat.getInstance("yyyy-MM-dd");

    public static String getMillisecondsFormattedAsString(long milliseconds) {
        long seconds = milliseconds / SECOND_IN_MS;
        long minutes = seconds / MINUTE_IN_SECONDS;
        long hours = minutes / HOUR_IN_MINUTES;
        long days = hours / DAY_IN_HOURS;
        StringBuilder sb = new StringBuilder();
        if (days > 0) {
            sb.append(days).append(" ").append("day").append(days == 1 ? "" : "s");
        }
        hours = hours % DAY_IN_HOURS;
        if (hours > 0) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(hours).append(" ").append("hour").append(hours == 1 ? "" : "s");
        }
        minutes = minutes % HOUR_IN_MINUTES;
        if (minutes > 0) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(minutes).append(" ").append("minute").append(minutes == 1 ? "" : "s");
        }
        seconds = seconds % MINUTE_IN_SECONDS;
        if (seconds > 0) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(seconds).append(" ").append("second").append(seconds == 1 ? "" : "s");
        }
        return sb.toString();
    }

    /**
     * This method is similar to the {@link org.apache.commons.lang.time.DateUtils} isSameDay method except that it only
     * compares the month and day of the month. e.g., 1980-06-30 and 2001-06-30 would match.
     *
     * @param date1 the first date, not altered, not null
     * @param date2 the second date, not altered, not null
     * @return
     * @throws IllegalArgumentException if either date is <code>null</code>
     */
    public static boolean isSameMonthAndDay(java.util.Date date1, java.util.Date date2) {
        if (date1 == null || date2 == null) {
            throw new IllegalArgumentException("The date must not be null");
        }
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date1);
        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(date2);

        return (cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH)) && (cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH));
    }

    public static Calendar getCalendarWithYearMonthAndDayOnDefaultTimezone(Calendar from) {
        assert from != null : "A seed calendar must be provided when a util.Date using the seeds year and month";

        Calendar to = Calendar.getInstance();
        // bl: set the day of month to 1 up front to ensure no overflow issues when setting the month
        to.set(Calendar.DAY_OF_MONTH, 1);
        to.set(Calendar.YEAR, from.get(Calendar.YEAR));
        to.set(Calendar.MONTH, from.get(Calendar.MONTH));
        to.set(Calendar.DAY_OF_MONTH, from.get(Calendar.DAY_OF_MONTH));

        return DateUtils.truncate(to, Calendar.DAY_OF_MONTH);
    }

    public static String getTodayMonthDayString() {
        return getMonthDayString(Calendar.getInstance());
    }

    public static String getMonthDayString(Calendar cal) {
        return getMonthDayString(cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH));
    }

    public static String getMonthDayString(int month, int dayOfMonth) {
        NumberFormat nf = NumberFormat.getNumberInstance();
        nf.setMinimumIntegerDigits(2);
        return nf.format(month) + nf.format(dayOfMonth);
    }

    // jw: the Google and Yahoo URL's require a strange format of UTC, no '/' or ':' and from GMT timezone.
    private static final FastDateFormat UTC_DATETIME_FORMAT = FastDateFormat.getInstance("yyyyMMdd'T'HHmmss", IPDateUtil.UTC_TIMEZONE, null);
    private static final FastDateFormat YYYYMMDD_DATE_FORMAT = FastDateFormat.getInstance("yyyyMMdd");

    public static String getYyyyMmDdString(Calendar calendar) {
        return YYYYMMDD_DATE_FORMAT.format(calendar);
    }

    public static String getUtcDatetimeString(Timestamp datetime) {
        return getUtcDatetimeString(datetime, false);
    }

    // jw: for some APIs (Google and Solr) the UTC formatted date has a Z at the end
    public static String getUtcDatetimeString(Timestamp datetime, boolean includeZ) {
        String formatted = UTC_DATETIME_FORMAT.format(datetime);

        if (includeZ) {
            return formatted + "Z";
        }

        return formatted;
    }

    // jw: Per fullcalendars documentation: http://fullcalendar.io/docs/utilities/Moment/
    private static final FastDateFormat ISO8601_UTC_DATETIME_FORMAT = FastDateFormat.getInstance("yyyy-MM-dd'T'HH:mm:ss", IPDateUtil.UTC_TIMEZONE);

    public static String getIso8601UtcDatetimeString(java.util.Date date) {
        return ISO8601_UTC_DATETIME_FORMAT.format(date);
    }

    // bl: note that JavaScript dates are inferred to be UTC when you include the 'T' in them, so we need
    // to display the date formatted in UTC time so that it ultimately displays at the proper time of day
    // based on the user's time zone
    private static final FastDateFormat JAVASCRIPT_DATE_FORMAT = FastDateFormat.getInstance("yyyy-MM-dd'T'HH:mm:ss", IPDateUtil.UTC_TIMEZONE);

    public static String getJavaScriptDateFormat(Calendar cal) {
        return JAVASCRIPT_DATE_FORMAT.format(cal);
    }

    private static final SimpleDateFormat DATETIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    public static java.util.Date parseIso8601Datetime(String datetime) {
        try {
            return DATETIME_FORMAT.parse(datetime);
        } catch (ParseException e) {
            return null;
        }
    }

    public static java.sql.Timestamp parseIso8601DatetimeAsTimestamp(String datetime) {
        java.util.Date parsed = parseIso8601Datetime(datetime);

        if (parsed != null) {
            new java.sql.Timestamp(parsed.getTime());
        }

        return null;
    }

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    public static java.util.Date parseIso8601Date(String datetime) {
        try {
            return DATE_FORMAT.parse(datetime);
        } catch (ParseException e) {
            return null;
        }
    }
}
