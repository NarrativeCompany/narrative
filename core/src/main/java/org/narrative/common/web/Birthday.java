package org.narrative.common.web;

import org.narrative.common.util.TimeZoneWrapper;
import org.apache.commons.lang3.time.DateUtils;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * A little Birthday class to handle marshalling/demarshalling from web requests and also
 * to simplify the getting of the month, day, and year fields of a birthday for display in the UI.
 * <p>
 * Date: Aug 11, 2006
 * Time: 7:40:09 AM
 *
 * @author Brian
 */
public class Birthday implements Serializable, Validatable {
    private int month = -1;
    private int dayOfMonth = -1;
    private int year = -1;
    private Date birthdayDate;
    private boolean isBirthdayDateSet = false;

    public Birthday() {
    }

    public Birthday(Date birthday) {
        if (birthday != null) {
            setFromDate(birthday);
        } else {
            birthdayDate = null;
        }
        isBirthdayDateSet = true;
    }

    public void setFromDate(Date date) {
        Calendar cal = Calendar.getInstance(TimeZoneWrapper.DEFAULT_TIME_ZONE);
        cal.setTime(date);
        month = cal.get(Calendar.MONTH);
        dayOfMonth = cal.get(Calendar.DAY_OF_MONTH);
        year = cal.get(Calendar.YEAR);
        cal = DateUtils.truncate(cal, Calendar.DAY_OF_MONTH);
        birthdayDate = cal.getTime();
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
        clearBirthdayDateInternal();
    }

    public int getDayOfMonth() {
        return dayOfMonth;
    }

    public void setDayOfMonth(int dayOfMonth) {
        this.dayOfMonth = dayOfMonth;
        clearBirthdayDateInternal();
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
        clearBirthdayDateInternal();
    }

    public Date getBirthdayDate() {
        if (!isBirthdayDateSet) {
            setBirthdayDateInternal();
        }
        return birthdayDate;
    }

    public boolean isValid() {
        return getBirthdayDate() != null;
    }

    public boolean isBirthdayTodayInTimeZone(TimeZone userTimeZone) {
        LocalDate now = LocalDate.now(userTimeZone.toZoneId());
        LocalDate bDay = toLocalDate();
        return now.getDayOfMonth() == bDay.getDayOfMonth() && now.getMonthValue() == bDay.getMonthValue();
    }

    public LocalDate toLocalDate() {
        return LocalDate.of(year, month + 1, dayOfMonth);
    }

    private void clearBirthdayDateInternal() {
        birthdayDate = null;
        isBirthdayDateSet = false;
    }

    private void setBirthdayDateInternal() {
        Date birthdayDateToUse = null;
        try {
            // bl: primitive validation up front
            if (year > 0 && month >= 0 && dayOfMonth >= 1) {
                Calendar cal = Calendar.getInstance(TimeZoneWrapper.DEFAULT_TIME_ZONE);
                cal.setLenient(false);
                // bl: set the day of month to 1 up front to ensure no overflow issues when setting the month
                cal.set(Calendar.DAY_OF_MONTH, 1);
                cal.set(Calendar.YEAR, year);
                cal.set(Calendar.MONTH, month);
                cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                cal = DateUtils.truncate(cal, Calendar.DAY_OF_MONTH);
                birthdayDateToUse = cal.getTime();
            }
        } catch (Throwable t) {
            // if the date is invalid, then just null out the birthdayDate
        }
        birthdayDate = birthdayDateToUse;
        isBirthdayDateSet = true;
    }
}
