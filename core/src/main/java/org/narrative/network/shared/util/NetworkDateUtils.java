package org.narrative.network.shared.util;

import org.narrative.common.persistence.ObjectPair;
import org.narrative.common.persistence.ObjectTriplet;
import org.narrative.common.util.IPDateUtil;
import org.narrative.common.util.LRUMap;
import org.narrative.network.core.user.services.preferences.FormatPreferences;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.commons.lang3.time.FastDateFormat;
import org.ocpsoft.pretty.time.PrettyTime;
import org.ocpsoft.pretty.time.TimeUnit;
import org.ocpsoft.pretty.time.units.Century;
import org.ocpsoft.pretty.time.units.Day;
import org.ocpsoft.pretty.time.units.Decade;
import org.ocpsoft.pretty.time.units.Hour;
import org.ocpsoft.pretty.time.units.Millennium;
import org.ocpsoft.pretty.time.units.Minute;
import org.ocpsoft.pretty.time.units.Month;
import org.ocpsoft.pretty.time.units.Second;
import org.ocpsoft.pretty.time.units.Week;
import org.ocpsoft.pretty.time.units.Year;

import java.text.DateFormat;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.narrative.common.util.CoreUtils.*;
import static org.narrative.network.shared.util.NetworkCoreUtils.*;

/**
 * Created by IntelliJ IDEA.
 * User: Paul
 * Date: Jan 24, 2006
 * Time: 11:44:43 AM
 */
public class NetworkDateUtils {

    private static final int TIME_STYLE = DateFormat.SHORT;

    private static final LRUMap<ObjectTriplet<TimeZone, Locale, Integer>, FastDateFormat> DATE_FORMAT_MAP = new LRUMap<ObjectTriplet<TimeZone, Locale, Integer>, FastDateFormat>();
    private static final LRUMap<ObjectTriplet<TimeZone, Locale, Integer>, FastDateFormat> DATETIME_FORMAT_MAP = new LRUMap<ObjectTriplet<TimeZone, Locale, Integer>, FastDateFormat>();
    private static final LRUMap<ObjectPair<TimeZone, Locale>, FastDateFormat> TIME_FORMAT_MAP = new LRUMap<ObjectPair<TimeZone, Locale>, FastDateFormat>();
    private static final LRUMap<ObjectTriplet<TimeZone, Locale, String>, FastDateFormat> CALENDAR_PATTERN_FORMAT_MAP = new LRUMap<ObjectTriplet<TimeZone, Locale, String>, FastDateFormat>();
    private static final ThreadLocal<PrettyTime> PRETTY_TIME_FORMATTER = new ThreadLocal<PrettyTime>();

    private static final String SHORT_DAY_OF_WEEK_FORMAT = "EEE";
    private static final String ISO8601_DATE_FORMAT = "yyyy-MM-dd";
    // jw: Per fullcalendars documentation: http://fullcalendar.io/docs/utilities/Moment/
    // bl: ZZ in FastDateFormat should be the -08:00 format required by ISO 8601
    private static final String ISO8601_DATETIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ssZZ";

    public static String dateFormatShortDayOfWeek(Date date) {
        return getFastDateFormat(SHORT_DAY_OF_WEEK_FORMAT).format(date);
    }

    public static String dateFormatShortDate(Date date) {
        return dateFormatShortDate(date, null);
    }

    public static String dateFormatShortDate(Date date, TimeZone fixedTimeZone) {
        return getDateFormatted(DateFormat.SHORT, fixedTimeZone, date);
    }

    public static String calendarFormatShortDate(Calendar date) {
        return getCalendarFormatted(DateFormat.SHORT, date);
    }

    public static String calendarFormatLongDate(Calendar date) {
        return getCalendarFormatted(DateFormat.LONG, date);
    }

    public static String dateFormatLongDate(Date date) {
        return dateFormatLongDate(date, null);
    }

    public static String dateFormatLongDate(Date date, TimeZone fixedTimeZone) {
        return getDateFormatted(DateFormat.LONG, fixedTimeZone, date);
    }

    private static String getDateFormatted(int dateFormat, TimeZone fixedTimeZone, Date date) {
        return getDateFormat(dateFormat, fixedTimeZone).format(date);
    }

    private static String getCalendarFormatted(int dateFormat, Calendar calendar) {
        return getDateFormat(dateFormat, null).format(calendar);
    }

    private static FastDateFormat getDateFormat(int dateFormat, TimeZone timeZone) {
        ObjectTriplet<TimeZone, Locale, Integer> key = getTimeZoneLocaleAndFormat(dateFormat, timeZone);
        FastDateFormat ret = DATE_FORMAT_MAP.get(key);
        if (ret != null) {
            return ret;
        }
        ret = FastDateFormat.getDateInstance(dateFormat, key.getOne(), key.getTwo());
        DATE_FORMAT_MAP.put(key, ret);
        return ret;
    }

    // jw: necessary for gn.tld
    public static String dateFormatShortDatetime(Date date) {
        return getDatetimeFormatted(DateFormat.SHORT, date, true, null, null);
    }

    public static String dateFormatShortDatetime(Date date, TimeZone fixedTimeZone, Boolean displayTimeZone) {
        return getDatetimeFormatted(DateFormat.SHORT, date, true, fixedTimeZone, displayTimeZone);
    }

    public static String dateFormatShortDatetimeNoPrettyTime(Date date) {
        return dateFormatShortDatetimeNoPrettyTime(date, null, null);
    }

    public static String dateFormatShortDatetimeNoPrettyTime(Date date, TimeZone fixedTimeZone, Boolean displayTimeZone) {
        return getDatetimeFormatted(DateFormat.SHORT, date, false, fixedTimeZone, displayTimeZone);
    }

    // jw: necessary for gn.tld
    public static String dateFormatLongDatetime(Date date) {
        return getDatetimeFormatted(DateFormat.LONG, date, true, null, null);
    }

    public static String dateFormatLongDatetime(Date date, Boolean displayTimeZone) {
        return dateFormatLongDatetime(date, null, displayTimeZone);
    }

    public static String dateFormatLongDatetime(Date date, TimeZone fixedTimeZone, Boolean displayTimeZone) {
        return getDatetimeFormatted(DateFormat.LONG, date, true, fixedTimeZone, displayTimeZone);
    }

    public static String dateFormatLongDatetimePretty(Date date) {
        return getDatetimePrettyFormatted(DateFormat.LONG, null, date);
    }

    public static String dateTitleLongString(Date date) {
        return getDateTitleString(DateFormat.LONG, null, date);
    }

    public static String dateFormatLongDatetimeNoPrettyTime(Date date) {
        return dateFormatLongDatetimeNoPrettyTime(date, null, null);
    }

    public static String dateFormatLongDatetimeNoPrettyTime(Date date, TimeZone fixedTimeZone, Boolean displayTimeZone) {
        return getDatetimeFormatted(DateFormat.LONG, date, false, fixedTimeZone, displayTimeZone);
    }

    public static boolean isDateWithinPrettyTimeFrame(Date date) {
        // bl: only want to use PrettyTime if the date is within the past 2 days.
        // don't use for future dates or anything older than 2 days.
        long diff = System.currentTimeMillis() - date.getTime();
        return diff >= 0 && diff <= (2 * IPDateUtil.DAY_IN_MS);
    }

    private static String getDatetimeFormatted(int dateFormat, Date date, boolean usePrettyTime, TimeZone fixedTimeZone, Boolean displayTimeZone) {
        if (date == null) {
            return null;
        }
        if (usePrettyTime && isDateWithinPrettyTimeFrame(date)) {
            return getDatetimePrettyFormatted(dateFormat, fixedTimeZone, date);
        }

        return getDateTimeWithTimeZone(date, getDatetimeFormat(dateFormat, fixedTimeZone).format(date), fixedTimeZone, displayTimeZone);
    }

    public static String datetimeFormatPattern() {
        return getDatetimeFormat(DateFormat.SHORT, null).getPattern();
    }

    public static String getDatetimePrettyFormatted(int dateFormat, TimeZone fixedTimeZone, Date date) {
        Locale locale = networkContext().getLocale();
        // bl: cache the PrettyTime formatter on the current thread. as long as the Locale is the same,
        // then we can continue to use the same existing PrettyTime object.
        PrettyTime prettyTime = PRETTY_TIME_FORMATTER.get();
        if (prettyTime == null || !isEqual(prettyTime.getLocale(), locale)) {
            prettyTime = new PrettyTime(locale);
            adjustMaximums(prettyTime);
            PRETTY_TIME_FORMATTER.set(prettyTime);
        }

        // bl: wrap the PrettyTime in a span with a title attribute that has the full datetime format string.
        StringBuilder sb = new StringBuilder();
        sb.append("<span title=\"");
        sb.append(getDateTitleString(dateFormat, fixedTimeZone, date));
        sb.append("\">");
        sb.append(prettyTime.format(date));
        sb.append("</span>");
        return sb.toString();
    }

    public static String getDateTitleString(int dateFormat, TimeZone fixedTimeZone, Date date) {
        return getDatetimeFormat(dateFormat, fixedTimeZone).format(date) + " " + getTimeZoneFormatted(TimeZone.SHORT, date, fixedTimeZone);
    }

    public static String formatShortTimeZone(Date date) {
        return getTimeZoneFormatted(TimeZone.SHORT, date, null);
    }

    public static String formatLongTimeZone(Date date) {
        return formatLongTimeZone(date, null);
    }

    public static String formatLongTimeZone(Date date, TimeZone timeZone) {
        return getTimeZoneFormatted(TimeZone.LONG, date, timeZone);
    }

    private static String getTimeZoneFormatted(int timeZoneFormat, Date date, TimeZone timeZone) {
        FormatPreferences prefs = networkContext().getFormatPreferences();
        if (timeZone != null) {
            prefs = new FormatPreferences(prefs.getLocale(), prefs.getLanguageLocale(), timeZone);
        }

        return prefs.getTimeZoneNameForDisplay(timeZoneFormat, date);
    }

    public static void adjustMaximums(PrettyTime prettyTime) {
        /*for (TimeUnit timeUnit : prettyTime.getUnits()) {
            if(timeUnit instanceof JustNow) {
                // bl: only want to show "moments ago" for up to 1 minute.  after that, we should show "2 minutes ago", etc.
                ((JustNow)timeUnit).setMaxQuantity(0);
            } else if(timeUnit instanceof Millisecond) {
                //((Millisecond)timeUnit).setMaxQuantity(0);
            }
        }*/
        Locale locale = prettyTime.getLocale();
        List<TimeUnit> timeUnits = new ArrayList<TimeUnit>(10);
        //timeUnits.add(new JustNow(locale));
        //timeUnits.add(new Millisecond(locale));
        timeUnits.add(new Second(locale));
        timeUnits.add(new Minute(locale));
        timeUnits.add(new Hour(locale));
        timeUnits.add(new Day(locale));
        timeUnits.add(new Week(locale));
        timeUnits.add(new Month(locale));
        timeUnits.add(new Year(locale));
        timeUnits.add(new Decade(locale));
        timeUnits.add(new Century(locale));
        timeUnits.add(new Millennium(locale));
        prettyTime.setUnits(timeUnits);
    }

    private static FastDateFormat getDatetimeFormat(int dateFormat, TimeZone timeZone) {
        ObjectTriplet<TimeZone, Locale, Integer> key = getTimeZoneLocaleAndFormat(dateFormat, timeZone);
        FastDateFormat ret = DATETIME_FORMAT_MAP.get(key);
        if (ret != null) {
            return ret;
        }
        ret = FastDateFormat.getDateTimeInstance(dateFormat, TIME_STYLE, key.getOne(), key.getTwo());
        DATETIME_FORMAT_MAP.put(key, ret);
        return ret;
    }

    public static String dateFormatTime(Date date) {
        return dateFormatTime(date, null, null);
    }

    public static String dateFormatTime(Date date, Boolean displayTimeZone) {
        return dateFormatTime(date, null, displayTimeZone);
    }

    public static String dateFormatTime(Date date, TimeZone fixedTimeZone, Boolean displayTimeZone) {
        return getDateTimeWithTimeZone(date, getTimeFormat(fixedTimeZone).format(date), fixedTimeZone, displayTimeZone);
    }

    private static String getDateTimeWithTimeZone(Date date, String value, TimeZone fixedTimeZone, Boolean displayTimeZone) {
        if (displayTimeZone == null) {
            return value;
        }
        if (displayTimeZone) {
            return value + " " + getTimeZoneFormatted(TimeZone.SHORT, date, fixedTimeZone);
        }

        return "<span title=\"" + getTimeZoneFormatted(TimeZone.LONG, date, fixedTimeZone) + "\">" + value + "</span>";
    }

    public static String hourAndMinuteFormatTime(int hour, int minute) {
        Calendar calendar = networkContext().getFormatPreferences().getCalendar();
        IPDateUtil.getCalendarTruncatedToDate(calendar);
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);

        return getTimeFormat(null).format(calendar);
    }

    private static FastDateFormat getTimeFormat(TimeZone fixedTimeZone) {
        ObjectPair<TimeZone, Locale> key = getTimeZoneAndLocale(fixedTimeZone);
        FastDateFormat ret = TIME_FORMAT_MAP.get(key);
        if (ret != null) {
            return ret;
        }
        ret = FastDateFormat.getTimeInstance(TIME_STYLE, key.getOne(), key.getTwo());
        TIME_FORMAT_MAP.put(key, ret);
        return ret;
    }

    private static ObjectPair<TimeZone, Locale> getTimeZoneAndLocale(TimeZone fixedTimeZone) {
        return new ObjectPair<TimeZone, Locale>(fixedTimeZone != null ? fixedTimeZone : networkContext().getFormatPreferences().getTimeZone(), networkContext().getLocale());
    }

    private static ObjectTriplet<TimeZone, Locale, Integer> getTimeZoneLocaleAndFormat(int dateFormat, TimeZone fixedTimeZone) {
        return new ObjectTriplet<TimeZone, Locale, Integer>(fixedTimeZone != null ? fixedTimeZone : networkContext().getFormatPreferences().getTimeZone(), networkContext().getLocale(), dateFormat);
    }

    private static ObjectTriplet<TimeZone, Locale, String> getTimeZoneLocaleAndPattern(String pattern) {
        return new ObjectTriplet<TimeZone, Locale, String>(networkContext().getFormatPreferences().getTimeZone(), networkContext().getLocale(), pattern);
    }

    private static final Pattern REMOVE_DAY_PATTERN = Pattern.compile("[^YyMm' ]*[Dd]+[^YyMm' ]*");
    private static final Pattern SPAIN_REMOVE_DAY_PATTERN = Pattern.compile("^[Dd]*' de '");
    private static final Pattern EXTRA_WHITESPACE_PATTERN = Pattern.compile("[ ]{2,}");
    private static final LRUMap<ObjectPair<TimeZone, Locale>, FastDateFormat> MONTH_YEAR_FORMAT_LOOKUP = new LRUMap<>();

    public static String dateFormatStaticLongMonthAndYear(Date date) {
        TimeZone timeZone = networkContext().getPrimaryRole().getFormatPreferences().getTimeZone();
        Locale locale = networkContext().getPrimaryRole().getFormatPreferences().getLocale();

        ObjectPair<TimeZone, Locale> key = new ObjectPair<>(timeZone, locale);
        FastDateFormat format = MONTH_YEAR_FORMAT_LOOKUP.get(key);
        if (format == null) {
            String datePattern = FastDateFormat.getDateInstance(DateFormat.LONG, locale).getPattern();
            Pattern stripper = datePattern.contains("de") ? SPAIN_REMOVE_DAY_PATTERN : REMOVE_DAY_PATTERN;

            // jw: let's massage the original pattern. Further, since we could be pulling the day off the beginning or end, we need to trim this to ensure that all
            //     whitespace only exists in the middle.
            datePattern = stripper.matcher(datePattern).replaceAll("").trim();

            // jw: now, let's remove any grouped up whitespace that may exist from where we stripped the day out.
            datePattern = EXTRA_WHITESPACE_PATTERN.matcher(datePattern).replaceAll(" ");

            format = FastDateFormat.getInstance(datePattern, timeZone, locale);
            MONTH_YEAR_FORMAT_LOOKUP.put(key, format);
        }

        return format.format(date);
    }

    public static String dateFormatIso8601Date(Date date) {
        return getFastDateFormat(ISO8601_DATE_FORMAT).format(date);
    }

    public static String calendarFormatIso8601Date(Calendar calendar) {
        return getFastDateFormat(ISO8601_DATE_FORMAT).format(calendar);
    }

    public static String getIso8601DatetimeString(java.util.Date date) {
        return getFastDateFormat(ISO8601_DATETIME_FORMAT).format(date);
    }

    // jw: to support month day formats, we will need to adjust the DateFormat.LONG formats and strip out the year from the standard format for the locale.
    private static final Pattern REMOVE_YEAR_PATTERN = Pattern.compile("[^DdMm']*[Yy]+[^DdMm']*");
    private static final Pattern SPAIN_REMOVE_YEAR_PATTERN = Pattern.compile("[^Mm]*[Yy]+[^Mm]*");
    private static final Pattern REMOVE_LEADING_ZERO_IN_DAY_PATTERN = Pattern.compile("dd+", Pattern.DOTALL);
    private static final Pattern DAY_PATTERN = Pattern.compile("d");
    private static final LRUMap<ObjectPair<TimeZone, Locale>, FastDateFormat> MONTH_DAY_FORMAT_LOOKUP = new LRUMap<>();

    public static String dateFormatLongMonthDayFormat(java.util.Date date) {
        TimeZone timeZone = networkContext().getPrimaryRole().getFormatPreferences().getTimeZone();
        Locale locale = networkContext().getPrimaryRole().getFormatPreferences().getLocale();

        ObjectPair<TimeZone, Locale> key = new ObjectPair<>(timeZone, locale);
        FastDateFormat format = MONTH_DAY_FORMAT_LOOKUP.get(key);
        if (format == null) {
            String datePattern = FastDateFormat.getDateInstance(DateFormat.LONG, locale).getPattern();
            Pattern stripper = datePattern.contains("de") ? SPAIN_REMOVE_YEAR_PATTERN : REMOVE_YEAR_PATTERN;

            String strippedPattern = stripper.matcher(datePattern).replaceAll("");

            //mk: Strip leading 0s in days, and add day suffix pattern (eg 8th)
            Matcher m = REMOVE_LEADING_ZERO_IN_DAY_PATTERN.matcher(strippedPattern);
            if (m.find()) {
                strippedPattern = m.replaceFirst("d");
            }
            m = DAY_PATTERN.matcher(strippedPattern);
            if (m.find()) {
                strippedPattern = m.replaceFirst("d'%s'");
            }
            format = FastDateFormat.getInstance(strippedPattern, timeZone, locale);
            MONTH_DAY_FORMAT_LOOKUP.put(key, format);
        }
        if (format.getPattern().contains("%s")) {
            Calendar cal = networkContext().getFormatPreferences().getCalendar();
            cal.setTime(date);
            return String.format(format.format(date), getDayOfMonthSuffix(cal.get(Calendar.DAY_OF_MONTH)));
        } else {
            return format.format(date);
        }
    }

    private static String getDayOfMonthSuffix(int n) {
        if (n < 1 || n > 31) {
            return "";
        }
        String toReturn = "";
        if (n >= 11 && n <= 13) {
            toReturn = "th";
        } else {
            switch (n % 10) {
                case 1:
                    toReturn = "st";
                    break;
                case 2:
                    toReturn = "nd";
                    break;
                case 3:
                    toReturn = "rd";
                    break;
                default:
                    toReturn = "th";
            }
        }
        return toReturn;
    }

    private static FastDateFormat getFastDateFormat(String pattern) {
        ObjectTriplet<TimeZone, Locale, String> key = getTimeZoneLocaleAndPattern(pattern);
        FastDateFormat ret = CALENDAR_PATTERN_FORMAT_MAP.get(key);
        if (ret != null) {
            return ret;
        }
        ret = FastDateFormat.getInstance(pattern, key.getOne(), key.getTwo());
        CALENDAR_PATTERN_FORMAT_MAP.put(key, ret);
        return ret;
    }

    public static boolean areDatesOnSameDay(Date date1, Date date2) {
        Calendar calendar1 = networkContext().getFormatPreferences().getCalendar();
        calendar1.setTime(date1);

        Calendar calendar2 = networkContext().getFormatPreferences().getCalendar();
        calendar2.setTime(date2);

        return areCalendarsOnSameDay(calendar1, calendar2);
    }

    public static boolean areCalendarsOnSameDay(Calendar calendar1, Calendar calendar2) {
        calendar1 = DateUtils.truncate(calendar1, Calendar.DAY_OF_MONTH);
        calendar2 = DateUtils.truncate(calendar2, Calendar.DAY_OF_MONTH);
        return calendar1.equals(calendar2);
    }

    public static boolean areDatesOnSameMonth(Date date1, Date date2) {
        Calendar calendar = networkContext().getFormatPreferences().getCalendar();

        calendar.setTime(date1);
        Calendar calendar1 = DateUtils.truncate(calendar, Calendar.MONTH);

        calendar.setTime(date2);
        Calendar calendar2 = DateUtils.truncate(calendar, Calendar.MONTH);

        return calendar1.equals(calendar2);
    }

    public static boolean isAmPmTimePattern() {
        FastDateFormat format = getTimeFormat(null);
        String timePattern = format.getPattern();
        if (isEmpty(timePattern)) {
            return false;
        }
        return timePattern.contains("a") || timePattern.contains("h") || timePattern.contains("K");
    }
}
