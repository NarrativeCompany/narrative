package org.narrative.common.web;

import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Date: Aug 31, 2006
 * Time: 12:43:09 PM
 *
 * @author Brian
 */
public enum DayOfWeek {
    SUNDAY(0x10, Calendar.SUNDAY),
    MONDAY(0x20, Calendar.MONDAY),
    TUESDAY(0x40, Calendar.TUESDAY),
    WEDNESDAY(0x80, Calendar.WEDNESDAY),
    THURSDAY(0x100, Calendar.THURSDAY),
    FRIDAY(0x200, Calendar.FRIDAY),
    SATURDAY(0x400, Calendar.SATURDAY);

    private static final Map<Integer, DayOfWeek> DAY_OF_WEEK_BY_CALENDAR_DAY;

    static {
        Map<Integer, DayOfWeek> calendarDayToDayOfWeek = new HashMap<Integer, DayOfWeek>();
        for (DayOfWeek dayOfWeek : DayOfWeek.values()) {
            calendarDayToDayOfWeek.put(dayOfWeek.getDayFromCalendar(), dayOfWeek);
        }
        DAY_OF_WEEK_BY_CALENDAR_DAY = Collections.unmodifiableMap(calendarDayToDayOfWeek);
    }

    private final int dayBitmask;
    private final int dayFromCalendar;

    private DayOfWeek(int dayBitmask, int dayFromCalendar) {
        this.dayBitmask = dayBitmask;
        this.dayFromCalendar = dayFromCalendar;
    }

    public int getDayBitmask() {
        return dayBitmask;
    }

    public int getDayFromCalendar() {
        return dayFromCalendar;
    }

    public boolean isMonday() {
        return this == MONDAY;
    }

    public boolean isTuesday() {
        return this == TUESDAY;
    }

    public boolean isWednesday() {
        return this == WEDNESDAY;
    }

    public boolean isThursday() {
        return this == THURSDAY;
    }

    public boolean isFriday() {
        return this == FRIDAY;
    }

    public boolean isSaturday() {
        return this == SATURDAY;
    }

    public boolean isSunday() {
        return this == SUNDAY;
    }

    public static DayOfWeek getDayOfWeekByCalendarDay(int day) {
        return DAY_OF_WEEK_BY_CALENDAR_DAY.get(day);
    }
}
