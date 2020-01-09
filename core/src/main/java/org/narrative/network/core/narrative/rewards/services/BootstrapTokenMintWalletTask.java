package org.narrative.network.core.narrative.rewards.services;

import org.narrative.network.core.narrative.wallet.TokenMintYear;
import org.narrative.network.core.narrative.wallet.Wallet;
import org.narrative.network.core.narrative.wallet.WalletType;
import org.narrative.network.shared.tasktypes.AreaTaskImpl;

/**
 * Date: 2019-05-27
 * Time: 09:26
 *
 * @author brian
 */
public class BootstrapTokenMintWalletTask extends AreaTaskImpl<Wallet> {
    @Override
    protected Wallet doMonitoredTask() {
        Wallet wallet = new Wallet(WalletType.TOKEN_MINT);

        Wallet.dao().save(wallet);

        // bl: record the first year token mint transaction. every subsequent year's mint will be triggered
        // at the end of the year.
        getAreaContext().doAreaTask(new RecordTokenMintTransactionForYear(TokenMintYear.YEAR_1));

        return wallet;
    }
}
