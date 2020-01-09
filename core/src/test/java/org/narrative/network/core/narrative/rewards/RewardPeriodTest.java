package org.narrative.network.core.narrative.rewards;

import org.narrative.network.core.area.base.ItemHourTrendingStats;
import org.narrative.network.core.narrative.rewards.services.RewardUtils;
import org.narrative.network.core.narrative.wallet.TokenMintYear;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.YearMonth;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Date: 2019-05-21
 * Time: 20:44
 *
 * @author jonmark
 */
class RewardPeriodTest {

    @Test
    void getNextMintYear_Success_withinSameYear() {
        testNextMintYear(TokenMintYear.YEAR_1, 1, TokenMintYear.YEAR_1);
    }

    @Test
    void getNextMintYear_Success_toNextYear() {
        testNextMintYear(TokenMintYear.YEAR_1, RewardUtils.MAX_MONTHLY_CAPTURES_PER_YEAR, TokenMintYear.YEAR_2);
    }

    @Test
    void getNextMintYear_Success_fromLastMonthOfLastYear() {
        testNextMintYear(TokenMintYear.YEAR_15, RewardUtils.MAX_MONTHLY_CAPTURES_PER_YEAR, null);
    }

    @Test
    void getNextMintYear_Success_fromNoMinting() {
        testNextMintYear(null, null, null);
    }

    private void testNextMintYear(TokenMintYear mintYear, Integer mintMonth, TokenMintYear expectedMintYear) {
        RewardPeriod period = new RewardPeriod(RewardUtils.nowYearMonth(), mintYear, mintMonth);

        assertEquals(period.getNextMintYear(), expectedMintYear);
    }

    @Test
    void getNextMintMonth_Success_withinSameYear() {
        testNextMintMonth(TokenMintYear.YEAR_1, 1, 2);
    }

    @Test
    void getNextMintMonth_Success_toNextYear() {
        testNextMintMonth(TokenMintYear.YEAR_1, RewardUtils.MAX_MONTHLY_CAPTURES_PER_YEAR, 1);
    }

    @Test
    void getNextMintMonth_Success_fromLastMonthOfLastYear() {
        testNextMintMonth(TokenMintYear.YEAR_15, RewardUtils.MAX_MONTHLY_CAPTURES_PER_YEAR, null);
    }

    @Test
    void getNextMintMonth_Success_fromNoMinting() {
        testNextMintMonth(null, null, null);
    }

    private void testNextMintMonth(TokenMintYear mintYear, Integer mintMonth, Integer expectedMintMonth) {
        RewardPeriod period = new RewardPeriod(RewardUtils.nowYearMonth(), mintYear, mintMonth);

        assertEquals(period.getNextMintMonth(), expectedMintMonth);
    }

    @Test
    void getLowerBoundForQuery_correct_forMay2019() {
        RewardPeriod period = new RewardPeriod(RewardUtils.FIRST_ACTIVE_YEAR_MONTH, null, null);
        Instant lowerBound = period.getRewardYearMonth().getLowerBoundForQuery();

        // bl: the expected lower bound for May 2019 is the epoch so it includes data from all time
        Instant epoch = Instant.EPOCH;

        test_getLowerBoundForQuery(lowerBound, epoch, 0);
    }

    @Test
    void getLowerBoundForQuery_correct_forJune2019() {
        RewardPeriod period = new RewardPeriod(YearMonth.of(2019, Month.JUNE), null, null);
        Instant lowerBound = period.getRewardYearMonth().getLowerBoundForQuery();

        Instant june12019 = LocalDateTime.of(2019, Month.JUNE, 1, 0, 0, 0, 0).toInstant(RewardUtils.REWARDS_ZONE_OFFSET);

        test_getLowerBoundForQuery(lowerBound, june12019, 433152);
    }

    private void test_getLowerBoundForQuery(Instant lowerBound, Instant expectedInstant, long expectedHour) {
        assertEquals(lowerBound, expectedInstant);
        assertEquals(ItemHourTrendingStats.getHoursSinceTheEpoch(lowerBound.toEpochMilli()), ItemHourTrendingStats.getHoursSinceTheEpoch(expectedInstant.toEpochMilli()));
        assertEquals(ItemHourTrendingStats.getHoursSinceTheEpoch(lowerBound.toEpochMilli()), expectedHour);
    }

    @Test
    void getUpperBoundForQuery_correct_forMay2019() {
        RewardPeriod period = new RewardPeriod(RewardUtils.FIRST_ACTIVE_YEAR_MONTH, null, null);
        Instant upperBound = period.getRewardYearMonth().getUpperBoundForQuery();

        // bl: the upper bound is an exclusive value, so it should be June 1
        Instant june12019 = LocalDateTime.of(2019, Month.JUNE, 1, 0, 0, 0, 0).toInstant(RewardUtils.REWARDS_ZONE_OFFSET);

        test_getUpperBoundForQuery(upperBound, june12019, 433152);
    }

    @Test
    void getUpperBoundForQuery_correct_forJune2019() {
        RewardPeriod period = new RewardPeriod(YearMonth.of(2019, Month.JUNE), null, null);
        Instant upperBound = period.getRewardYearMonth().getUpperBoundForQuery();

        // bl: the upper bound is an exclusive value, so it should be July 1
        Instant july12019 = LocalDateTime.of(2019, Month.JULY, 1, 0, 0, 0, 0).toInstant(RewardUtils.REWARDS_ZONE_OFFSET);

        // bl: 433152 is the hour for June 1, 2019, so the upper bound should be July 1, which is 30 24 hour periods
        test_getUpperBoundForQuery(upperBound, july12019, 433152+(24*30));
    }

    private void test_getUpperBoundForQuery(Instant upperBound, Instant expectedInstant, long expectedHour) {
        assertEquals(upperBound, expectedInstant);
        assertEquals(ItemHourTrendingStats.getHoursSinceTheEpoch(upperBound.toEpochMilli()), ItemHourTrendingStats.getHoursSinceTheEpoch(expectedInstant.toEpochMilli()));
        assertEquals(ItemHourTrendingStats.getHoursSinceTheEpoch(upperBound.toEpochMilli()), expectedHour);
    }

    @Test
    void getLowerBoundForUi_correct_forMay2019() {
        RewardPeriod period = new RewardPeriod(RewardUtils.FIRST_ACTIVE_YEAR_MONTH, null, null);
        LocalDate lowerBound = period.getRewardYearMonth().getLowerBoundForUi();

        // bl: the expected lower bound for May 2019 is April 2, the date the beta was launched
        LocalDate april2 = LocalDate.of(2019, Month.APRIL, 2);

        test_getLowerBoundForUi(lowerBound, april2);
    }

    @Test
    void getLowerBoundForUi_correct_forJune2019() {
        RewardPeriod period = new RewardPeriod(YearMonth.of(2019, Month.JUNE), null, null);
        LocalDate lowerBound = period.getRewardYearMonth().getLowerBoundForUi();

        LocalDate june1 = LocalDate.of(2019, Month.JUNE, 1);

        test_getLowerBoundForUi(lowerBound, june1);
    }

    private void test_getLowerBoundForUi(LocalDate lowerBound, LocalDate expectedDate) {
        assertEquals(lowerBound, expectedDate);
    }

    @Test
    void getUpperBoundForUi_correct_forMay2019() {
        RewardPeriod period = new RewardPeriod(RewardUtils.FIRST_ACTIVE_YEAR_MONTH, null, null);
        LocalDate upperBound = period.getRewardYearMonth().getUpperBoundForUi();

        // bl: the expected upper bound for May 2019 is May 31
        LocalDate may31 = LocalDate.of(2019, Month.MAY, 31);

        test_getUpperBoundForUi(upperBound, may31);
    }

    @Test
    void getUpperBoundForUi_correct_forJune2019() {
        RewardPeriod period = new RewardPeriod(YearMonth.of(2019, Month.JUNE), null, null);
        LocalDate upperBound = period.getRewardYearMonth().getUpperBoundForUi();

        // bl: the expected upper bound for May 2019 is May 31
        LocalDate june30 = LocalDate.of(2019, Month.JUNE, 30);

        test_getUpperBoundForUi(upperBound, june30);
    }

    private void test_getUpperBoundForUi(LocalDate upperBound, LocalDate expectedDate) {
        assertEquals(upperBound, expectedDate);
    }

}