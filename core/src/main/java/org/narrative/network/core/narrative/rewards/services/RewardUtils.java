package org.narrative.network.core.narrative.rewards.services;

import org.narrative.common.util.IPDateUtil;
import org.narrative.network.customizations.narrative.NrveValue;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Date: 2019-05-17
 * Time: 15:13
 *
 * @author jonmark
 */
public class RewardUtils {
    public static final TimeZone REWARDS_TIME_ZONE = IPDateUtil.UTC_TIMEZONE;
    public static final ZoneOffset REWARDS_ZONE_OFFSET = ZoneOffset.UTC;

    /**
     * bl: we want to run the reward processing after 5 full days have completed, so we'll process rewards at the start
     * of the 6th day of the month
     */
    public static final int REWARD_DAY_OF_MONTH = 6;

    public static final RoundingMode ROUNDING_MODE = RoundingMode.DOWN;

    // jw: Couple of helpful constants
    public static final YearMonth APRIL_2019 = YearMonth.of(2019, Month.APRIL);
    public static final YearMonth MAY_2019 = YearMonth.of(2019, Month.MAY);

    // jw: these are minimums for RewardPeriod dates (April 2nd, which is what we will be reporting as the start of the first RewardsPeriod (Mays))
    public static final YearMonth FIRST_ACTIVE_YEAR_MONTH = MAY_2019;
    public static final LocalDate FIRST_REWARDS_DAY = APRIL_2019.atDay(2);

    public static final int MAX_MONTHLY_CAPTURES_PER_YEAR = 12;

    public static NrveValue calculateNrveShare(NrveValue nrve, long dividend, long divisor) {
        assert nrve != null : "Nrve should always be provided";
        assert dividend >= 0 : "Should never be provided with negative dividend or divisor";
        assert divisor >= dividend : "The divisor should never be smaller than the dividend.";

        // jw: short out if the dividend is 0
        if (dividend == 0 || divisor == 0) {
            return NrveValue.ZERO;
        }


        // jw: if they are the same we know that the share is the whole thing.
        if (dividend == divisor) {
            return nrve;
        }

        // jw: if the pool is 0 then their share will always be zero.
        if (nrve.compareTo(NrveValue.ZERO) == 0) {
            return NrveValue.ZERO;
        }

        // jw: finally, we can multiply this out.
        return nrve.multiply(dividend).divide(divisor, ROUNDING_MODE);
    }

    public static NrveValue calculateNrveShare(BigDecimal percentage, NrveValue totalNrve, RoundingMode roundingMode) {
        assert percentage != null : "Should always have a percentage.";
        assert percentage.compareTo(BigDecimal.ZERO) >= 0 : "Should never have a negative percentage.";
        assert percentage.compareTo(BigDecimal.ONE) <= 0 : "Should never have a percentage greater than 100.";
        assert totalNrve != null : "Should always have a totalNrve value!";

        return totalNrve.multiply(percentage, roundingMode);
    }

    // jw: there is at least one case where we need to distribute a pool of NRVE across a series of objects based on each objects
    //     share, where the share is calculated by summing each items NRVE values together and then calculating each objects
    //     percentage of that total NRVE amount.
    public static <T> void distributeNrveProportionally(NrveValue nrve, Collection<T> items, Function<T, NrveValue> getNrveFunction, BiConsumer<T, NrveValue> addNrveFunction) {
        assert nrve != null : "nrve should never be null.";
        assert getNrveFunction != null : "We should always have a function to derive the NRVE from an item.";
        assert addNrveFunction != null : "We should always have a function to apply the items share of the NRVE pool.";

        // jw: if we do not have any items or nrve to distribute, then skip out!
        if (isEmptyOrNull(items) || nrve.compareTo(NrveValue.ZERO) == 0) {
            return;
        }

        NrveValue totalNrveForItems = NrveValue.ZERO;

        // jw: use a linked hash map to preserve order.
        Map<T, NrveValue> itemNrveLookup = new LinkedHashMap<>(items.size());
        for (T item : items) {
            NrveValue itemNrve = getNrveFunction.apply(item);

            // jw: for now, all item NRVE values should not be negative.
            assert itemNrve.compareTo(NrveValue.ZERO) >= 0 : "Should always get a NRVE amount.";

            totalNrveForItems = totalNrveForItems.add(itemNrve);
            itemNrveLookup.put(item, itemNrve);
        }

        // jw: now that we have the total NRVE for all items, let's ensure that if the pool we are aiming to distribute
        //     is negative that the total NRVE for items can cover it.
        assert nrve.compareTo(NrveValue.ZERO) > 0 || totalNrveForItems.add(nrve).compareTo(NrveValue.ZERO) >= 0 : "The totalNrveForItems should always be greater to or equal to any negative pool value.";

        NrveValue nrveDistributed = NrveValue.ZERO;
        Iterator<Map.Entry<T, NrveValue>> entryIterator = itemNrveLookup.entrySet().iterator();
        while (entryIterator.hasNext()) {
            // jw: let's first get the next entry, and then remove it.
            Map.Entry<T, NrveValue> entry = entryIterator.next();
            entryIterator.remove();

            T item = entry.getKey();

            // jw: if this is the last item then just give them what is left in the pool.
            if (itemNrveLookup.isEmpty()) {
                addNrveFunction.accept(item, nrve.subtract(nrveDistributed));

            // jw: since this is not the last person we need to calculate their share and multiply it out.
            } else {
                NrveValue itemNrve = entry.getValue();

                NrveValue share = calculateNrveShare(nrve, itemNrve.toNeurons(), totalNrveForItems.toNeurons());

                addNrveFunction.accept(item, share);
                nrveDistributed = nrveDistributed.add(share);
            }
        }
    }

    public static YearMonth calculateYearMonth(Date date) {
        assert date != null : "Date should always be provided.";

        return calculateYearMonth(Instant.ofEpochMilli(date.getTime()));
    }

    public static NrveValue calculatePerCaptureValue(NrveValue totalValue, int totalCaptures) {
        assert totalValue != null : "We should always have a value, even if it is ZERO!";
        assert totalCaptures > 0 : "What is the point of calling this if we do not have any captures to make?";

        return calculateNrveShare(totalValue, 1, totalCaptures);
    }

    public static NrveValue calculateCaptureValue(NrveValue totalValue, int capture, int totalCaptures) {
        return calculateCaptureValue(
                totalValue,
                calculatePerCaptureValue(totalValue, totalCaptures),
                capture,
                totalCaptures
        );
    }

    // jw: there is a lot of faith going into this function, since all values must be calculated in advance and then passed
    //     in on for each call.
    public static NrveValue calculateCaptureValue(NrveValue totalValue, NrveValue perCaptureValue, int capture, int totalCaptures) {
        assert totalValue != null : "We should always be given a totalValue object";
        assert perCaptureValue != null : "We should always be given a perCaptureValue object";
        assert capture > 0 : "The capture we are fetching should always be greater than 0";
        assert capture <= totalCaptures : "The capture should always be less than or equal to the total number of captures.";

        // jw: if we only have one capture then let's just short out since there is no need for all the mumbo jumbo below.
        if (totalCaptures == 1) {
            return totalValue;
        }

        // jw: if the total is ZERO, let's also short out.
        if (totalValue.compareTo(NrveValue.ZERO) == 0) {
            return NrveValue.ZERO;
        }

        assert Math.signum(totalValue.compareTo(NrveValue.ZERO)) == Math.signum(totalValue.compareTo(perCaptureValue)) : "totalValue should always have the same relationship to zero as it has with perCaptureValue.";

        if (capture == totalCaptures) {
            // jw: if this is the last capture let's return everything that has not already been distributed. To do that,
            //     let's subtract the total amount captured up to this point from the total, ensuring that whatever rounding
            //     has caused some neurons to be left behind will be left over appropriately.
            NrveValue valueCaptured = perCaptureValue.multiply(totalCaptures - 1);

            // jw: the logic for this assert is different based on whether the total is negative or not.
            assert totalValue.compareTo(NrveValue.ZERO) > 0
                    ? valueCaptured.compareTo(totalValue) < 0
                    : valueCaptured.compareTo(totalValue) > 0 : "We should always have some value left after calculating what has already been captured!";

            return totalValue.subtract(valueCaptured);
        }

        // jw: if we are not the last capture let's trust the perCaptureValue and return that.
        return perCaptureValue;
    }

    public static YearMonth calculateYearMonth(Instant instant) {
        assert instant != null : "The moment of time to calculate the YearMonth from must be provided.";
        assert instant.compareTo(Instant.now()) <= 0 : "We should never calculating YearMonth values for the future!";

        YearMonth yearMonth = YearMonth.from(instant.atZone(REWARDS_ZONE_OFFSET));
        // bl: the oldest YearMonth we should ever use is May 2019
        if (yearMonth.isBefore(FIRST_ACTIVE_YEAR_MONTH)) {
            return FIRST_ACTIVE_YEAR_MONTH;
        }

        return yearMonth;
    }

    public static YearMonth nowYearMonth() {
        return YearMonth.now(REWARDS_ZONE_OFFSET);
    }
}
