package org.narrative.network.core.narrative.rewards.services;

import org.narrative.common.util.IPDateUtil;
import org.narrative.network.core.narrative.wallet.Wallet;
import org.narrative.network.core.narrative.wallet.WalletTransaction;
import org.narrative.network.core.narrative.wallet.WalletTransactionStatus;
import org.narrative.network.core.narrative.wallet.WalletTransactionType;
import org.narrative.network.core.narrative.wallet.WalletType;
import org.narrative.network.customizations.narrative.NrveValue;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Date: 2019-05-17
 * Time: 15:23
 *
 * @author jonmark
 */
class RewardUtilsTest {

    // Test names should follow the standard: MethodName_StateUnderTest_ExpectedBehavior

    // jw: due to the immense number of decimal characters we are keeping I need this utility to make life a bit easier here.
    private BigDecimal buildExpectedDecimalValue(String base, String repeatingCharacter) {
        StringBuilder expectedValue = new StringBuilder("0.");
        if (base!=null) {
            expectedValue.append(base);
        }

        // jw: adding two to offset the "0." prefix on this string.
        while (expectedValue.length() < NrveValue.SCALE + 2) {
            expectedValue.append(repeatingCharacter);
        }

        return new BigDecimal(expectedValue.toString());
    }

    @Test
    void calculateNrveShare_Success_ZeroDividend() {
        assertEquals(RewardUtils.calculateNrveShare(new NrveValue(BigDecimal.ONE), 0, 3), NrveValue.ZERO);
    }

    @Test
    void calculateNrveShare_Success_OneHundredPercent() {
        assertEquals(RewardUtils.calculateNrveShare(new NrveValue(BigDecimal.ONE), 3, 3), new NrveValue(BigDecimal.ONE));
    }

    @Test
    void calculateNrveShare_Success_RoundedValue() {
        assertEquals(RewardUtils.calculateNrveShare(buildExpectedDecimalValue(null, "3"), new NrveValue(BigDecimal.ONE), RewardUtils.ROUNDING_MODE), new NrveValue(33333333L));
        assertEquals(RewardUtils.calculateNrveShare(new NrveValue(BigDecimal.ONE), 1, 3), new NrveValue(33333333L));
    }

    @Test
    void calculateNrveShare_UnexpectedError_PercentageAboveOne() {
        assertThrows(AssertionError.class, () -> RewardUtils.calculateNrveShare(new BigDecimal("1.3"), new NrveValue(BigDecimal.ONE), RoundingMode.UNNECESSARY));
        assertThrows(AssertionError.class, () -> RewardUtils.calculateNrveShare(new NrveValue(BigDecimal.ONE), 2, 1));
    }

    @Test
    void calculateNrveShare_UnexpectedError_NegativeDividend() {
        assertThrows(AssertionError.class, () -> RewardUtils.calculateNrveShare(new NrveValue(BigDecimal.ONE), -1, 1));
    }

    @Test
    void calculateNrveShare_CorrectValue_LongArithmeticOverflow() {
        assertEquals(new NrveValue(new BigDecimal("10743.25707309")), RewardUtils.calculateNrveShare(new NrveValue(149947587899999L), 78194, 10913824));
    }

    @Test
    void calculateYearMonth_Success_ThisMonth() {
        assertEquals(RewardUtils.calculateYearMonth(new Date()), RewardUtils.nowYearMonth());
    }

    @Test
    void calculateYearMonth_Success_MinCatchall() {
        assertEquals(RewardUtils.calculateYearMonth(new Date(0)), RewardUtils.MAY_2019);
    }

    @Test
    void calculateYearMonth_UnexpectedError_FutureYearMonth() {
        assertThrows(AssertionError.class, () -> RewardUtils.calculateYearMonth(new Date(System.currentTimeMillis() + IPDateUtil.YEAR_IN_MS)));
    }

    @Test
    void calculatePerCaptureValue_Success_calculateRoundedValue() {
        // jw: 3 NRVE 1 Neuron divided by 3 should result in 1 NRVE.
        assertEquals(
                RewardUtils.calculatePerCaptureValue(new NrveValue(300000001L), 3),
                NrveValue.ONE
        );
    }

    @Test
    void calculatePerCaptureValue_Success_calculateInfiniteRoundedValue() {
        // jw: 1 NRVE divided by 3 should result in .3333... NRVE.
        assertEquals(
                RewardUtils.calculatePerCaptureValue(NrveValue.ONE, 3),
                new NrveValue(33333333L)
        );
    }

    @Test
    void calculatePerCaptureValue_Success_calculateNegativeValue() {
        // jw: 1 NRVE divided by 3 should result in .3333... NRVE.
        assertEquals(
                RewardUtils.calculatePerCaptureValue(new NrveValue(-300000001L), 3),
                new NrveValue(BigDecimal.valueOf(-1))
        );
    }

    @Test
    void calculatePerCaptureValue_UnexpectedError_inputValues() {
        assertThrows(AssertionError.class, () -> RewardUtils.calculatePerCaptureValue(new NrveValue(1000000L), 0));
        assertThrows(AssertionError.class, () -> RewardUtils.calculatePerCaptureValue(null, 10));
    }

    @Test
    void calculateCaptureValue_Success_roundedLeadingValue() {
        // jw: 3 NRVE 1 Neuron divided by 3 should result in 1 NRVE.
        assertEquals(
                RewardUtils.calculateCaptureValue(new NrveValue(300000001L), NrveValue.ONE, 1, 3),
                NrveValue.ONE
        );
    }

    @Test
    void calculateCaptureValue_Success_remainderTrailingValue() {
        // jw: 3 NRVE 1 Neuron divided by 3 should result in 1 NRVE.
        assertEquals(
                RewardUtils.calculateCaptureValue(new NrveValue(300000001L), NrveValue.ONE, 3, 3),
                new NrveValue(100000001L)
        );
    }

    @Test
    void calculateCaptureValue_UnexpectedError_inputValues() {
        assertThrows(AssertionError.class, () -> RewardUtils.calculateCaptureValue(
                null,
                new NrveValue(1000000L),
                1,
                10
        ), "totalValue being null should have caused an assertion error!");
        assertThrows(AssertionError.class, () -> RewardUtils.calculateCaptureValue(
                new NrveValue(1000000L),
                null,
                1,
                10
        ), "perCaptureValue being null should have caused an assertion error!");
        assertThrows(AssertionError.class, () -> RewardUtils.calculateCaptureValue(
                new NrveValue(1000000L),
                new NrveValue(1000001L),
                1,
                10
        ), "perCaptureValue being greater than a positive totalValue should have caused an assertion error!");
        assertThrows(AssertionError.class, () -> RewardUtils.calculateCaptureValue(
                new NrveValue(-1000000L),
                new NrveValue(-1000001L),
                1,
                10
        ), "perCaptureValue being less than a negative totalValue should have caused an assertion error!");
        assertThrows(AssertionError.class, () -> RewardUtils.calculateCaptureValue(
                new NrveValue(10L),
                new NrveValue(1L),
                0,
                10
        ), "Having a 0 capture should have caused an assertion error!");
        assertThrows(AssertionError.class, () -> RewardUtils.calculateCaptureValue(
                new NrveValue(10L),
                new NrveValue(1L),
                2,
                1
        ), "Having a capture value greater than totalCaptures should have caused an assertion error!");
        assertThrows(AssertionError.class, () -> RewardUtils.calculateCaptureValue(
                new NrveValue(10L),
                new NrveValue(5L),
                3,
                3
        ), "Having a last capture value calculated that is greater than totalValue should have caused an assertion error!");
    }

    @Test
    void distributeNrveProportionally_Success_distributedValues() {
        class TestObject {
            private NrveValue value;

            public TestObject(NrveValue value) {
                this.value = value;
            }

            public NrveValue getValue() {
                return value;
            }

            public void addValue(NrveValue value) {
                this.value = this.value.add(value);
            }
        }

        // jw: the total for all of these objects is 10 NRVE
        TestObject one = new TestObject(new NrveValue(100000000L));
        TestObject two = new TestObject(new NrveValue(200000000L));
        TestObject three = new TestObject(new NrveValue(300000000L));
        TestObject four = new TestObject(new NrveValue(400000000L));

        RewardUtils.distributeNrveProportionally(
                // jw: the amount we will be distributing is 20 NRVE 3 Neurons.
                new NrveValue(2000000003L),
                Arrays.asList(one, two, three, four),
                TestObject::getValue,
                TestObject::addValue
        );

        // jw: since all of the transactions were a percentage of each objects share of the 10 NRVE total, this should be
        //     easy to predict.
        assertEquals(new NrveValue(300000000L), one.value);
        assertEquals(new NrveValue(600000000L), two.value);
        assertEquals(new NrveValue(900000000L), three.value);
        // jw: note: since this is the last object it should have picked up the remainder.
        assertEquals(new NrveValue(1200000003L), four.value);
    }

    @Test
    void distributeWalletNrveProportionally_Success_distributedValues() {
        Wallet wallet = new Wallet(WalletType.NICHE_MONTH_REVENUE);
        wallet.setBalance(new NrveValue(30989655000000L));

        NrveValue originalBalance = wallet.getBalance();

        List<Long> transactionAmounts = Arrays.asList(
                959002000000L,
                877856000000L,
                1238443000000L,
                1147649000000L,
                980545000000L,
                1148602000000L,
                980545000000L,
                1089741000000L,
                906125000000L,
                2000100000000L,
                898997000000L,
                618587000000L,
                1147649000000L,
                500642000000L,
                500652000000L,
                737029000000L,
                500652000000L,
                600100000000L,
                535784000000L,
                565377000000L,
                557525000000L,
                460755000000L,
                460755000000L,
                460755000000L,
                460755000000L,
                460755000000L,
                461435000000L,
                503335000000L,
                477520000000L
        );

        List<WalletTransaction> transactions = new ArrayList<>(transactionAmounts.size());
        for (Long transactionAmount : transactionAmounts) {
            transactions.add(new WalletTransaction(null, wallet, WalletTransactionType.NICHE_PAYMENT, WalletTransactionStatus.PENDING_FIAT_ADJUSTMENT, new NrveValue(transactionAmount)));
        }

        NrveValue adjustment = new NrveValue(new BigDecimal("196929").subtract(new BigDecimal("222376.67")));

        RewardUtils.distributeNrveProportionally(
                adjustment,
                transactions,
                WalletTransaction::getNrveAmount,
                WalletTransaction::adjustFiatValue
        );

        assertEquals(0, wallet.getBalance().subtract(originalBalance).compareTo(adjustment));
    }
}