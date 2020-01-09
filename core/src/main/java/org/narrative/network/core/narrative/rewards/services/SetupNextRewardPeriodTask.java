package org.narrative.network.core.narrative.rewards.services;

import org.narrative.network.core.narrative.rewards.RewardPeriod;
import org.narrative.network.shared.tasktypes.AreaTaskImpl;

import java.time.YearMonth;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Date: 2019-05-21
 * Time: 19:55
 *
 * @author jonmark
 */
public class SetupNextRewardPeriodTask extends AreaTaskImpl<RewardPeriod> {
    public SetupNextRewardPeriodTask() {
        super(true);
    }

    @Override
    protected RewardPeriod doMonitoredTask() {
        RewardPeriod latestPeriod = RewardPeriod.dao().getLatestRewardPeriod();
        assert exists(latestPeriod) : "We should always have a rewardPeriod for the current month when this is called.";
        assert !latestPeriod.getPeriod().isAfter(RewardUtils.nowYearMonth()) : "The latestPeriod should never be in the future when this is called. For initial setup it will likely be in the past (by one month, but for more iterations it should be the current month.";

        // jw: now that we know we are processing let's setup the next month from the latest one.
        YearMonth nextMonth = latestPeriod.getPeriod().plusMonths(1);

        // jw: first things first, let's create the reward period.
        RewardPeriod nextPeriod = getAreaContext().doAreaTask(new CreateRewardPeriodTask(
                nextMonth,
                latestPeriod.getNextMintYear(),
                latestPeriod.getNextMintMonth())
        );
        assert exists(nextPeriod) : "We should always come out with a new RewardPeriod after calling the above task.";

        // jw: now with that done, let's create the ProratedMonthRevenue for that Month
        // jw: similar to RewardPeriod above, let's use this task to ensure that everything is processed in order.
        getAreaContext().doAreaTask(new CreateProratedMonthRevenuesTask(nextMonth));

        return nextPeriod;
    }
}
