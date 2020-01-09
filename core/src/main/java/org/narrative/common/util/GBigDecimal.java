package org.narrative.common.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Date: Feb 9, 2009
 * Time: 11:24:14 AM
 *
 * @author brian
 */
public class GBigDecimal {

    public static final GBigDecimal ZERO = new GBigDecimal(BigDecimal.ZERO);

    private final BigDecimal value;

    public GBigDecimal(BigDecimal value) {
        this.value = value;
    }

    public GBigDecimal(Double value) {
        this(BigDecimal.valueOf(value));
    }

    public BigDecimal getValue() {
        return value;
    }

    public String getFormattedAsUsd() {
        return NumberFormat.getCurrencyInstance(Locale.US).format(value);
    }

    public String getFormattedAsOneDecimalPercentage() {
        return formatBigDecimal(value, RoundingMode.FLOOR, 1, false, true) + "%";
    }

    public String getRoundedToOneDecimal() {
        return formatBigDecimal(value, RoundingMode.HALF_UP, 1, false, false);
    }

    public String getFormattedWithThreeDecimalsForInput() {
        return formatBigDecimal(value, RoundingMode.FLOOR, 3, false, false);
    }

    public String getFormattedAsTwoDecimalPercentage() {
        return formatBigDecimal(value, RoundingMode.FLOOR, 2, false, true) + "%";
    }

    public String getFormattedWithTwoDecimals() {
        return formatBigDecimal(value, RoundingMode.HALF_UP, 2, false, true);
    }

    public String getFormattedWithGroupingsAndTwoDecimals() {
        return formatBigDecimal(value, RoundingMode.HALF_UP, 2, true, true);
    }

    public int getIntValue() {
        return value.intValue();
    }

    public int getSignum() {
        return value.signum();
    }

    public double getDoubleValue() {
        return value.doubleValue();
    }

    public boolean isZero() {
        // bl: don't use BigDecimal.equals to compare values, as that will return false if the values are the same,
        // but the scale of the BigDecimal objects are different.
        return BigDecimal.ZERO.compareTo(value) == 0;
    }

    public static NumberFormat getDecimalFormat(int decimals, boolean includeGroupings, boolean zeroPadDecimals) {
        // jw: currently, we are going to default all numbers off of US number formatting.
        return getDecimalFormat(Locale.US, decimals, includeGroupings, zeroPadDecimals);
    }

    public static NumberFormat getDecimalFormat(Locale locale, int decimals, boolean includeGroupings, boolean zeroPadDecimals) {
        // jw: due to issues with sharing objects between threads, let's just create a new number format each time.
        NumberFormat numberFormat = NumberFormat.getNumberInstance(locale);
        numberFormat.setGroupingUsed(includeGroupings);
        numberFormat.setMaximumFractionDigits(decimals);
        // jw: some formats require that we want to show the full number of decimals, even when they are zero, so let's
        //     accommodate that.
        if (zeroPadDecimals) {
            numberFormat.setMinimumFractionDigits(decimals);
        }

        return numberFormat;
    }

    public static String formatBigDecimal(BigDecimal value, RoundingMode roundingMode, int decimals, boolean includeGroupings, boolean zeroPadDecimals) {
        // jw: first, lets ensure that the big decimal is rounded where we want it to be.
        value = value.setScale(decimals, roundingMode);

        // jw: if we are not including groupings we can just rely on the toPlainString method which should give us exactly what we want.
        if (!includeGroupings && zeroPadDecimals) {
            return value.toPlainString();
        }

        // jw: finally, let's format that number, now that it's where we expect it to be.
        return getDecimalFormat(decimals, includeGroupings, zeroPadDecimals).format(value);
    }

    public static GBigDecimal calculatePercentage(int count, int totalCount) {
        if (count == 0) {
            return ZERO;
        }

        assert count <= totalCount : "The count should always be less than or equal to to totalCount";

        return new GBigDecimal(BigDecimal.valueOf(count).divide(BigDecimal.valueOf(totalCount), 4, BigDecimal.ROUND_FLOOR).multiply(BigDecimal.valueOf(100)));
    }
}
