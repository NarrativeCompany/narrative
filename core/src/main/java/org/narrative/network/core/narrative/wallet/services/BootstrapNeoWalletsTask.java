package org.narrative.network.core.narrative.wallet.services;

import org.narrative.network.core.narrative.wallet.NeoWallet;
import org.narrative.network.core.narrative.wallet.NeoWalletType;
import org.narrative.network.core.narrative.wallet.WalletType;
import org.narrative.network.shared.tasktypes.AreaTaskImpl;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Date: 2019-06-11
 * Time: 11:30
 *
 * @author brian
 */
public class BootstrapNeoWalletsTask extends AreaTaskImpl<Object> {
    @Override
    protected Object doMonitoredTask() {
        // bl: only need to bootstrap singleton wallets that do not have a corresponding Wallet
        for (NeoWalletType type : NeoWalletType.SINGLETON_TYPES) {
            // bl: types that have a Wallet will get their NeoWallet created when the corresponding Wallet is created
            WalletType walletType = type.getWalletType();
            if(walletType!=null && walletType.isSameCardinalityAsNeoWalletType()) {
                continue;
            }
            NeoWallet neoWallet = new NeoWallet(type);

            NeoWallet.dao().save(neoWallet);
        }

        return null;
    }
}
