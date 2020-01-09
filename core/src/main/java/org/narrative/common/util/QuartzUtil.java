package org.narrative.common.util;

import org.quartz.CronScheduleBuilder;
import org.quartz.DateBuilder;
import org.quartz.TriggerBuilder;

import java.util.TimeZone;

import static org.quartz.CronScheduleBuilder.*;
import static org.quartz.SimpleScheduleBuilder.*;
import static org.quartz.TriggerBuilder.*;

/**
 * Date: 2/23/16
 * Time: 10:39 AM
 *
 * @author brian
 */
public class QuartzUtil {

    public static TriggerBuilder makeSecondlyTrigger(int intervalInSeconds) {
        return newTrigger().withSchedule(
                // bl: use the CalendarIntervalScheduleBuilder for secondly triggers so that it doesn't
                // always occur at the same second of each minute, as with the DailyTimeIntervalScheduleBuilder.
                // this way, jobs that are run on the same interval on multiple servlets won't be guaranteed
                // to run at the same time, as previously.
                // bl: actually, changing back to using a SimpleScheduleBuilder now since that maintains
                // exactly the behavior that we had previously.
                repeatSecondlyForever(intervalInSeconds));
    }

    public static TriggerBuilder makeMinutelyTrigger(int intervalInMinutes) {
        return newTrigger().withSchedule(
                // bl: use the CalendarIntervalScheduleBuilder for minutely triggers so that it doesn't
                // always occur at the same minute of each hour, as with the DailyTimeIntervalScheduleBuilder.
                // this way, jobs that are run on the same interval on multiple servlets won't be guaranteed
                // to run at the same time, as previously.
                // bl: actually, changing back to using a SimpleScheduleBuilder now since that maintains
                // exactly the behavior that we had previously.
                repeatMinutelyForever(intervalInMinutes));
    }

    public static TriggerBuilder makeHourlyTrigger(int intervalInHours) {
        return newTrigger().withSchedule(repeatHourlyForever(intervalInHours));
    }

    public static TriggerBuilder makeHourlyOnMinuteTrigger(int minute) {
        DateBuilder.validateMinute(minute);
        return newTrigger().withSchedule(cronSchedule(String.format("0 %d * ? * *", minute)));
    }

    public static TriggerBuilder makeDailyTrigger(int hour, int minute) {
        return newTrigger().withSchedule(dailyAtHourAndMinute(hour, minute));
    }

    public static TriggerBuilder makeDailyTrigger(int hour, int minute, TimeZone timezone) {
        return newTrigger().withSchedule(dailyAtHourAndMinute(hour, minute).inTimeZone(timezone));
    }

    public static TriggerBuilder makeWeeklyTrigger(int dayOfWeek, int hour, int minute) {
        return newTrigger().withSchedule(weeklyOnDayAndHourAndMinute(dayOfWeek, hour, minute));
    }

    public static TriggerBuilder makeMonthlyTrigger(int dayOfMonth, int hour, int minute) {
        return makeMonthlyTrigger(dayOfMonth, hour, minute, null);
    }

    public static TriggerBuilder makeMonthlyTrigger(int dayOfMonth, int hour, int minute, TimeZone timeZone) {
        CronScheduleBuilder cronScheduleBuilder = monthlyOnDayAndHourAndMinute(dayOfMonth, hour, minute);
        if(timeZone!=null) {
            cronScheduleBuilder.inTimeZone(timeZone);
        }
        return newTrigger().withSchedule(cronScheduleBuilder);
    }
}
