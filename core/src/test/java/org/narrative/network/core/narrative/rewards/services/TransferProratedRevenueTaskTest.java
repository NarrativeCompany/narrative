package org.narrative.network.core.narrative.rewards.services;

import org.narrative.network.core.narrative.rewards.ProratedMonthRevenue;
import org.narrative.network.core.narrative.rewards.ProratedRevenueType;
import org.narrative.network.core.narrative.rewards.RewardPeriod;
import org.narrative.network.core.narrative.wallet.dao.WalletTransactionDAO;
import org.narrative.network.customizations.narrative.NrveValue;
import org.narrative.network.shared.context.AreaContext;
import org.narrative.network.shared.tasktypes.AreaTaskImpl;
import mockit.Expectations;
import mockit.Mocked;
import mockit.Verifications;

/**
 * Date: 2019-05-21
 * Time: 14:35
 *
 * @author jonmark
 */
class TransferProratedRevenueTaskTest {

    @Mocked
    WalletTransactionDAO transactionDAO;

    @Mocked
    AreaContext areaContext;

    @Mocked
    AreaTaskImpl areaTask;

    // jw:todo:3081: I have spent more than an hour trying to get this to work and cannot figure out a good way.

    //@Test
    void doMonitoredTask_Success_transferingMidRangeCapture() {
        testCapture(6);
    }

    //@Test
    void doMonitoredTask_Success_transferingLastCapture() {
        testCapture(RewardUtils.MAX_MONTHLY_CAPTURES_PER_YEAR);
    }

    private void testCapture(int capture) {
        NrveValue expectedChange = capture == RewardUtils.MAX_MONTHLY_CAPTURES_PER_YEAR
                            ? new NrveValue(100000011L)
                            : new NrveValue(100000000L);

        for (ProratedRevenueType revenueType : ProratedRevenueType.values()) {
            RewardPeriod period = new RewardPeriod(RewardUtils.nowYearMonth(), null, null);
            ProratedMonthRevenue revenue = new ProratedMonthRevenue(revenueType, period.getPeriod().minusMonths(capture - 1));
            revenue.setCaptures(capture - 1);
            // jw: using twelve thousand NRVE and elevent Neurons to ensure that we get some good rounding here.
            revenue.getWallet().setBalance(new NrveValue(1200000011L));

            // jw: Okay, create the task
            TransferProratedRevenueTask task = new TransferProratedRevenueTask(revenue, period);

            new Expectations(){{
                // jw: ignore the call to areaContext
                areaContext.doAreaTask((AreaTaskImpl) any);
                result = null;

                areaTask.getAreaContext();
                result = areaContext;
            }};

            new Verifications() {{
                AreaTaskImpl transactionTask;
                areaContext.doAreaTask(transactionTask = withCapture());

                boolean test = true;

/*
                assertEquals(fromWallet, revenue.getWallet(), "The fromWallet should be the same as the revenue we are pulling from.");
                assertEquals(toWallet, period.getWallet(), "The toWallet should be the same as the period we are transfering to.");
                assertEquals(transactionType, revenueType.getCaptureTransactionType(), "Expected a different transaction type to be used!");

                assertEquals(amount, expectedChange);
                assertEquals(period.getWallet().getBalance(), expectedChange);
*/
            }};

            // jw: and fake a run
            task.doMonitoredTask();

        }
    }
}