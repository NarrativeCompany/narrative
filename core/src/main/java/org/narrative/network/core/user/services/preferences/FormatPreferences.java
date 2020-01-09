package org.narrative.network.core.user.services.preferences;

import org.narrative.common.persistence.hibernate.IntegerEnumType;
import org.narrative.common.util.IPDateUtil;
import org.narrative.common.util.TimeZoneWrapper;
import org.narrative.network.core.settings.global.services.translations.DefaultLocale;
import org.hibernate.annotations.Type;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Transient;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by IntelliJ IDEA.
 * User: paul
 * Date: May 3, 2006
 * Time: 1:23:27 PM
 */
@Embeddable
public class FormatPreferences {

    public static FormatPreferences getDefaultFormatPreferences() {
        return getDefaultFormatPreferences(null);
    }

    public static FormatPreferences getDefaultFormatPreferences(DefaultLocale languageLocale) {
        return new FormatPreferences(DefaultLocale.getDefaultLocale(), languageLocale, TimeZoneWrapper.DEFAULT_TIME_ZONE);
    }

    private Locale locale;
    private DefaultLocale languageLocale;
    private TimeZone timeZone;

    @Deprecated
    public FormatPreferences() {
    }

    public FormatPreferences(Locale locale, DefaultLocale languageLocale, TimeZone timeZone) {
        this.timeZone = timeZone;
        this.languageLocale = languageLocale;
        this.locale = locale;
    }

    @Column(nullable = false)
    public TimeZone getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(TimeZone timeZone) {
        this.timeZone = timeZone;
    }

    @Transient
    public Calendar getCalendar() {
        return Calendar.getInstance(getTimeZone());
    }

    @Transient
    public String getTimeZoneNameForDisplay(int timeZoneFormat, Date forDate) {
        return getTimeZoneNameForDisplay(getTimeZone(), timeZoneFormat, forDate);
    }

    @Transient
    public String getTimeZoneNameForDisplay(TimeZone timeZone, int timeZoneFormat, Date forDate) {
        return timeZone.getDisplayName(timeZone.inDaylightTime(forDate)
                // bl: always display UTC in short format ("UTC") as opposed to "Coordinated Universal Time" (which fewer people will recognize)
                , timeZone.equals(IPDateUtil.UTC_TIMEZONE) ? TimeZone.SHORT : timeZoneFormat, getLocale());
    }

    @Transient
    public ZoneId getZoneId() {
        return getTimeZone().toZoneId();
    }

    @Column(nullable = false, columnDefinition = "varchar(8)")
    public Locale getLocale() {
        return locale;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    @Type(type = IntegerEnumType.TYPE)
    public DefaultLocale getLanguageLocale() {
        return languageLocale;
    }

    public void setLanguageLocale(DefaultLocale languageLocale) {
        this.languageLocale = languageLocale;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        FormatPreferences that = (FormatPreferences) o;

        if (languageLocale != that.languageLocale) {
            return false;
        }
        if (locale != null ? !locale.equals(that.locale) : that.locale != null) {
            return false;
        }
        if (timeZone != null ? !timeZone.equals(that.timeZone) : that.timeZone != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = locale != null ? locale.hashCode() : 0;
        result = 31 * result + (languageLocale != null ? languageLocale.hashCode() : 0);
        result = 31 * result + (timeZone != null ? timeZone.hashCode() : 0);
        return result;
    }
}
