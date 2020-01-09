package org.narrative.network.core.narrative.rewards.services;

import org.narrative.network.core.narrative.rewards.RewardPeriod;
import org.narrative.network.core.narrative.wallet.TokenMintYear;
import org.narrative.network.shared.tasktypes.AreaTaskImpl;

import java.time.YearMonth;

/**
 * Date: 2019-05-20
 * Time: 09:25
 *
 * @author jonmark
 */
public class CreateRewardPeriodTask extends AreaTaskImpl<RewardPeriod> {

    private final YearMonth yearMonth;
    private final TokenMintYear mintYear;
    private final Integer mintMonth;

    public CreateRewardPeriodTask(YearMonth yearMonth, TokenMintYear mintYear, Integer mintMonth) {
        super(true);
        assert yearMonth != null : "Should always have a yearMonth to creat a reward period for.";
        assert yearMonth.compareTo(RewardUtils.FIRST_ACTIVE_YEAR_MONTH) >= 0 : "Should never create a RewardPeriod for before May, 2019";
        assert (mintYear==null) == (mintMonth==null) : "Both or Neither mintYear and mintMonth must be specified, never one without the other.";

        this.yearMonth = yearMonth;
        this.mintYear = mintYear;
        this.mintMonth = mintMonth;
    }

    @Override
    protected RewardPeriod doMonitoredTask() {
        // jw: let's allow the RewardPeriod to setup itself (which includes instantiating a Wallet).
        RewardPeriod period = new RewardPeriod(yearMonth, mintYear, mintMonth);

        // jw: now we can just save the period
        RewardPeriod.dao().save(period);

        return period;
    }
}
