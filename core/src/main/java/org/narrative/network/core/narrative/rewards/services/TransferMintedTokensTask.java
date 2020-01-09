package org.narrative.network.core.narrative.rewards.services;

import org.narrative.common.util.UnexpectedError;
import org.narrative.network.core.narrative.rewards.RewardPeriod;
import org.narrative.network.core.narrative.wallet.TokenMintYear;
import org.narrative.network.core.narrative.wallet.Wallet;
import org.narrative.network.core.narrative.wallet.WalletTransaction;
import org.narrative.network.core.narrative.wallet.WalletTransactionType;
import org.narrative.network.core.narrative.wallet.services.ProcessWalletTransactionTask;
import org.narrative.network.shared.tasktypes.AreaTaskImpl;

import java.util.List;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Date: 2019-05-21
 * Time: 15:49
 *
 * @author jonmark
 */
public class TransferMintedTokensTask extends AreaTaskImpl<WalletTransaction> {
    private final RewardPeriod period;

    public TransferMintedTokensTask(RewardPeriod period) {
        this.period = period;

        assert exists(period) : "A RewardPeriod must be provided";
    }

    @Override
    protected WalletTransaction doMonitoredTask() {
        assert (period.getMintYear() == null) == (period.getMintMonth() == null) : "Every period should have either both mintYear/Month, or neither.";

        // jw: if the period does not have a mint, then let's go ahead and skip it.
        if (period.getMintYear() == null) {
            return null;
        }

        Wallet tokenMintWallet = Wallet.dao().getTokenMintWallet();

        // jw: if the period does have a mint we need to be sure to check to make sure we have not already transferred it.
        List<WalletTransaction> previousTransfers = WalletTransaction.dao().getForWalletsAndType(
                tokenMintWallet,
                period.getWallet(),
                WalletTransactionType.MINTED_TOKENS
        );
        if (!previousTransfers.isEmpty()) {
            // jw: allow MAY_2019 through as long as it does not have more than one capture. This is necessary due to how
            //     the patches are minting April's tokens into May's wallet.
            if (!period.getPeriod().equals(RewardUtils.FIRST_ACTIVE_YEAR_MONTH) || previousTransfers.size() > 1) {
                throw UnexpectedError.getRuntimeException("We should never have already processed a transfer from the token mint to a period prior to calling this task!");
            }
        }

        // jw: finally, we can process the transfer to this reward period.
        WalletTransaction transaction = getAreaContext().doAreaTask(new ProcessWalletTransactionTask(
                tokenMintWallet,
                period.getWallet(),
                WalletTransactionType.MINTED_TOKENS,
                // jw: the capture for this period is the number of its month within the year of captures for the mint
                period.getMintYear().getTokensForCapture(period.getMintMonth())
        ));

        // bl: now that we're done, let's check to see if we need to mint new tokens for the next month. if the next
        // month's year is different than this month's, then we have new tokens to mint!
        TokenMintYear nextMonthMintYear = period.getNextMintYear();
        if(nextMonthMintYear!=null && !isEqual(nextMonthMintYear, period.getMintYear())) {
            getAreaContext().doAreaTask(new RecordTokenMintTransactionForYear(nextMonthMintYear));
        }

        return transaction;
    }
}
