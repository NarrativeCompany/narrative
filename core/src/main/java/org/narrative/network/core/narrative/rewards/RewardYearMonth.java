package org.narrative.network.core.narrative.rewards;

import org.narrative.network.core.narrative.rewards.services.RewardUtils;

import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

/**
 * Date: 2019-05-26
 * Time: 19:34
 *
 * @author brian
 */
public class RewardYearMonth {
    public static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("MMMM yyyy");
    public static final DateTimeFormatter MONTH_NAME_FORMATTER = DateTimeFormatter.ofPattern("MMMM");

    private static final DateTimeFormatter MONTH_DAY_YEAR_FORMATTER = DateTimeFormatter.ofPattern("MMMM d, yyyy");
    private static final DateTimeFormatter MONTH_DAY_FORMATTER = DateTimeFormatter.ofPattern("MMMM d");

    private final YearMonth yearMonth;
    private final YearMonth firstActiveMonth;

    public RewardYearMonth(YearMonth yearMonth, YearMonth firstActiveMonth) {
        assert yearMonth!=null : "Should never construct a RewardYearMonth without a value!";
        this.yearMonth = yearMonth;
        this.firstActiveMonth = firstActiveMonth;
    }

    public String getFormatted() {
        return yearMonth.format(FORMATTER);
    }

    public boolean isBeforeNow() {
        return yearMonth.isBefore(RewardUtils.nowYearMonth());
    }

    public Instant getLowerBoundForQuery() {
        // bl: for the first month (May 2019), we'll go all the way back to the epoch for the query so that we
        // include all prior activity.
        if(!firstActiveMonth.isBefore(yearMonth)) {
            return Instant.EPOCH;
        }
        // for all months after May 2019, the first day for querying if the first of the month in UTC.
        return getLowerBoundForUi().atStartOfDay().toInstant(RewardUtils.REWARDS_ZONE_OFFSET);
    }

    public Instant getUpperBoundForQuery() {
        // bl: the upper bound is always the end of the last day of the month
        // effectively using the start of the next day (UTC), and then we'll just use this as an exclusive bound.
        return getUpperBoundForUi().plusDays(1).atStartOfDay().toInstant(RewardUtils.REWARDS_ZONE_OFFSET);
    }

    public LocalDate getLowerBoundForUi() {
        // bl: special case for the first month. for May 2019, we use the first rewards day of April 2, which is the day
        // that the Narrative beta was released
        if(!firstActiveMonth.isBefore(yearMonth)) {
            return RewardUtils.FIRST_REWARDS_DAY;
        }
        // bl: for all future months, the lower bound is just the first of the month
        return yearMonth.atDay(1);
    }

    public LocalDate getUpperBoundForUi() {
        // bl: the upper bound for the UI is always the last day of the month
        return yearMonth.atEndOfMonth();
    }

    public String getRewardPeriodRange() {
        return getLowerBoundForUi().format(MONTH_DAY_FORMATTER) + " - " + getUpperBoundForUi().format(MONTH_DAY_YEAR_FORMATTER);
    }
}
