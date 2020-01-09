package org.narrative.network.core.narrative.rewards.services;

import org.narrative.common.persistence.OID;
import org.narrative.common.util.UnexpectedError;
import org.narrative.network.core.narrative.rewards.ProratedMonthRevenue;
import org.narrative.network.core.narrative.rewards.ProratedRevenueType;
import org.narrative.network.core.narrative.rewards.RewardPeriod;
import org.narrative.network.core.narrative.rewards.RewardPeriodStep;
import org.narrative.network.core.narrative.wallet.TokenMintYear;
import org.narrative.network.core.versioning.services.PatchRunner;
import org.narrative.network.customizations.narrative.NrveValue;
import org.narrative.network.shared.tasktypes.AreaTaskImpl;
import org.narrative.network.shared.tasktypes.TaskRunner;

import java.time.YearMonth;

/**
 * Date: 2019-05-27
 * Time: 08:48
 *
 * @author brian
 */
public class BootstrapRewardPeriodsTask extends AreaTaskImpl<Object> {
    @Override
    protected Object doMonitoredTask() {
        YearMonth firstMonth = RewardUtils.FIRST_ACTIVE_YEAR_MONTH;
        // bl: first, create the ProratedMonthRevenues and RewardPeriod in an isolated transaction. this is needed
        // so that during NetworkInstall, when isolated transactions run
        TaskRunner.doRootAreaTask(getAreaContext().getArea().getOid(), new AreaTaskImpl<Object>() {
            @Override
            protected Object doMonitoredTask() {
                YearMonth specialMonth = firstMonth.minusMonths(1);
                // bl: first things first, handle the special case of April 2019, which only gets ProratedMonthRevenue, but no RewardPeriod
                getAreaContext().doAreaTask(new CreateProratedMonthRevenuesTask(specialMonth));

                // bl: special case to apply a fiat adjustment of 0 so that we won't have any revenue to capture for April.
                for (ProratedRevenueType revenueType : ProratedRevenueType.ACTIVE_TYPES) {
                    getAreaContext().doAreaTask(new ApplyFiatAdjustmentTask(
                            ProratedMonthRevenue.dao().getForYearMonthAndType(specialMonth, revenueType),
                            NrveValue.ZERO
                    ));
                }

                // now, we need to create the initial RewardPeriod for May 2019
                RewardPeriod mayPeriod = getAreaContext().doAreaTask(new CreateRewardPeriodTask(firstMonth, TokenMintYear.YEAR_1, 2));

                // bl: also need to create ProratedMonthRevenue records for May 2019
                getAreaContext().doAreaTask(new CreateProratedMonthRevenuesTask(firstMonth));

                // bl: now, handle April's special case where we transfer April 2019's token mint into May 2019
                getAreaContext().doAreaTask(new TransferMintedTokensTask(mayPeriod));

                // bl: finally, we need to set up the next month's (June's) RewardPeriod so it's in place and ready as part of the usual Rewards processing
                getAreaContext().doAreaTask(new SetupNextRewardPeriodTask());
                return null;
            }
        });

        // todo:post-v1.1.0 remove this if(isPatching) once the SetupAprilMayRewardPeriodsPatch is gone. it should
        // only run during install at that point, which is handled in the else.
        if(PatchRunner.isPatching()) {
            // bl: if we are not installing, then we're running for a patch, which means that we need to
            // just setup the RewardPeriod, but not process. this should handle setting up the June RewardPeriod
            // bl: note that we only need to do this for months _after_ May 2019. we already created May 2019 above
            // bl: finally, we need to process rewards for all months in the past since may
            YearMonth yearMonth = firstMonth;
            assert yearMonth.equals(RewardUtils.MAY_2019) : "Should only setup next reward period for June 2019 via patch! Not yearMonth/" + yearMonth;
            // jw: pretty easy, just run the task to create the next reward period.
            getAreaContext().doAreaTask(new SetupNextRewardPeriodTask());
        } else {
            // bl: finally, we need to process rewards for all months in the past since may
            YearMonth thisMonth = RewardUtils.nowYearMonth();
            YearMonth yearMonth = firstMonth;

            // bl: loop and process all prior months
            while(yearMonth.isBefore(thisMonth)) {
                OID rewardPeriodOid = RewardPeriod.dao().getOidForYearMonth(yearMonth);
                if(rewardPeriodOid==null) {
                    throw UnexpectedError.getRuntimeException("Failed identifying reward period for yearMonth/" + yearMonth);
                }

                // bl: do this in a separate transaction so that the values get committed before we start processing
                // the actual rewards. necessary in order for things to process properly.
                TaskRunner.doRootAreaTask(getAreaContext().getArea().getOid(), new AreaTaskImpl<Object>() {
                    @Override
                    protected Object doMonitoredTask() {
                        RewardPeriod rewardPeriod = RewardPeriod.dao().get(rewardPeriodOid);
                        // bl: first, we must apply the fiat adjustment value of 0
                        for (ProratedRevenueType revenueType : ProratedRevenueType.ACTIVE_TYPES) {
                            getAreaContext().doAreaTask(new ApplyFiatAdjustmentTask(
                                    ProratedMonthRevenue.dao().getForYearMonthAndType(rewardPeriod.getPeriod(), revenueType),
                                    NrveValue.ZERO
                            ));
                        }

                        // bl: mark the processing job as scheduled since we're running it directly
                        rewardPeriod.getCompletedSteps().add(RewardPeriodStep.SCHEDULE_PROCESSING_JOB);
                        return null;
                    }
                });

                // bl: just process rewards for the month. should be a no-op effectively, but will at least
                // cause the NRVE to be minted properly. this will also cause the next rewardPeriod to be created
                // as part of normal processing
                TaskRunner.doRootAreaTask(getAreaContext().getArea().getOid(), new AreaTaskImpl<Object>() {
                    @Override
                    protected Object doMonitoredTask() {
                        RewardPeriod rewardPeriod = RewardPeriod.dao().get(rewardPeriodOid);
                        getAreaContext().doAreaTask(new ProcessRewardPeriodTask(rewardPeriod));
                        return null;
                    }
                });

                yearMonth = yearMonth.plusMonths(1);
            }
        }

        return null;
    }
}
