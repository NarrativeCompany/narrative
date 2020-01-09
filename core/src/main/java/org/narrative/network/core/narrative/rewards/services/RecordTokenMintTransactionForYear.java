package org.narrative.network.core.narrative.rewards.services;

import org.narrative.common.util.UnexpectedError;
import org.narrative.network.core.narrative.wallet.TokenMintYear;
import org.narrative.network.core.narrative.wallet.Wallet;
import org.narrative.network.core.narrative.wallet.WalletTransaction;
import org.narrative.network.core.narrative.wallet.WalletTransactionType;
import org.narrative.network.core.narrative.wallet.services.ProcessWalletTransactionTask;
import org.narrative.network.shared.tasktypes.AreaTaskImpl;

import java.math.BigDecimal;
import java.util.List;

/**
 * Date: 2019-06-11
 * Time: 07:49
 *
 * @author brian
 */
public class RecordTokenMintTransactionForYear extends AreaTaskImpl<WalletTransaction> {
    private final TokenMintYear mintYear;

    public RecordTokenMintTransactionForYear(TokenMintYear mintYear) {
        assert mintYear!=null : "Should always specify a mint year!";
        this.mintYear = mintYear;
    }

    @Override
    protected WalletTransaction doMonitoredTask() {
        Wallet wallet = Wallet.dao().getTokenMintWallet();
        if(!wallet.getBalance().equals(BigDecimal.ZERO)) {
            throw UnexpectedError.getRuntimeException("The token mint wallet should always have a zero balance when minting the next year's tokens! mintYear/" + mintYear + " balance/" + wallet.getBalance());
        }

        // bl: check to make sure we haven't transferred this many tokens before. relies on the fact that each mint year
        // has a different amount of tokens
        List<WalletTransaction> previousTransfers = WalletTransaction.dao().getForToWalletTypeAndAmount(wallet, WalletTransactionType.INITIAL_TOKEN_MINT, mintYear.getTotalTokens());

        if(!previousTransfers.isEmpty()) {
            throw UnexpectedError.getRuntimeException("Attempted to mint tokens for the same year multiple times!");
        }

        WalletTransaction transaction = getAreaContext().doAreaTask(new ProcessWalletTransactionTask(
                null,
                wallet,
                WalletTransactionType.INITIAL_TOKEN_MINT,
                mintYear.getTotalTokens()
        ));

        if (!wallet.getBalance().equals(mintYear.getTotalTokens())) {
            throw UnexpectedError.getRuntimeException("Expected the token mint wallet to have a balance of " + mintYear.getTotalTokens() + " NRVE after processing. not/" + wallet.getBalance().getValue());
        }

        return transaction;
    }
}
