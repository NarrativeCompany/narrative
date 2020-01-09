package org.narrative.network.core.narrative.rewards.services.slices;

import org.narrative.network.core.narrative.rewards.RewardPeriod;
import org.narrative.network.core.narrative.rewards.RewardSlice;
import org.narrative.network.core.narrative.wallet.Wallet;
import org.narrative.network.core.narrative.wallet.WalletTransaction;
import org.narrative.network.customizations.narrative.NrveValue;
import org.narrative.network.shared.tasktypes.AreaTaskImpl;

/**
 * This task exists primarily to inject the RewardPeriod into each root task that we use to process rewards
 * so that each implementation doesn't have to worry about reloading the RewardPeriod into the current session.
 * When using anonymous implementations of this class within a RewardSliceProcessorBase, this `period` variable
 * will hide the RewardSliceProcessorBase.period, which is exactly what we're trying to accomplish here.
 *
 * Date: 2019-05-30
 * Time: 10:10
 *
 * @author brian
 */
public abstract class RewardSliceRootAreaTask<T> extends AreaTaskImpl<T> {
    private final RewardSlice slice;
    protected RewardPeriod period;

    public RewardSliceRootAreaTask(RewardSlice slice) {
        this.slice = slice;
    }

    WalletTransaction createTransaction(Wallet toWallet, NrveValue amount) {
        return getAreaContext().doAreaTask(new ProcessRewardDistributionTransactionTask(period, toWallet, slice, amount));
    }

    void setPeriod(RewardPeriod period) {
        this.period = period;
    }
}
