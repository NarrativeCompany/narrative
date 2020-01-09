package org.narrative.network.core.narrative.rewards.services.slices;

import org.narrative.network.core.narrative.rewards.RewardPeriod;
import org.narrative.network.core.narrative.rewards.RewardSlice;
import org.narrative.network.core.narrative.wallet.Wallet;
import org.narrative.network.core.narrative.wallet.WalletTransaction;
import org.narrative.network.core.narrative.wallet.services.ProcessWalletTransactionTask;
import org.narrative.network.customizations.narrative.NrveValue;
import org.narrative.network.shared.tasktypes.AreaTaskImpl;

/**
 * Date: 2019-05-30
 * Time: 08:31
 *
 * @author brian
 */
public class ProcessRewardDistributionTransactionTask extends AreaTaskImpl<WalletTransaction> {
    private final RewardPeriod period;
    private final Wallet toWallet;
    private final RewardSlice slice;
    private final NrveValue amount;

    ProcessRewardDistributionTransactionTask(RewardPeriod period, Wallet toWallet, RewardSlice slice, NrveValue amount) {
        // bl: note that we do allow zero amounts to be recorded
        assert amount.compareTo(NrveValue.ZERO) >= 0 : "Should never distribute negative amounts! period/" + period.getPeriod() + " toWallet/" + toWallet.getOid() + " slice/" + slice + " amount/" + amount;
        this.period = period;
        this.toWallet = toWallet;
        this.slice = slice;
        this.amount = amount;
    }

    @Override
    protected WalletTransaction doMonitoredTask() {
        WalletTransaction transaction = getAreaContext().doAreaTask(new ProcessWalletTransactionTask(period.getWallet(), toWallet, slice.getWalletTransactionType(), amount));
        // bl: now that we have recorded the transaction, we also need to update the RewardPeriod to update
        // the total amount that has been disbursed.
        // note that no further locking should be required since we will have already locked the RewardPeriod's wallet
        // when recording the transaction.
        period.setTotalRewardsDisbursed(period.getTotalRewardsDisbursed().add(amount));
        return transaction;
    }
}
