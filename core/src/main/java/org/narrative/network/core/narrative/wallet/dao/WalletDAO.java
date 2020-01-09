package org.narrative.network.core.narrative.wallet.dao;

import org.narrative.common.persistence.NameValuePair;
import org.narrative.common.persistence.OID;
import org.narrative.network.core.narrative.wallet.NeoWallet;
import org.narrative.network.core.narrative.wallet.Wallet;
import org.narrative.network.core.narrative.wallet.WalletType;
import org.narrative.network.customizations.narrative.NrveValue;
import org.narrative.network.shared.daobase.GlobalDAOImpl;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Date: 2019-05-14
 * Time: 15:31
 *
 * @author jonmark
 */
public class WalletDAO extends GlobalDAOImpl<Wallet, OID> {
    public WalletDAO() {
        super(Wallet.class);
    }

    public Wallet getTokenMintWallet() {
        return getSingletonWallet(WalletType.TOKEN_MINT);
    }

    public Wallet getSingletonWallet(WalletType type) {
        assert type.isSingleton() : "Should only use singleton types with this method! not type/" + type;
        Wallet wallet = getUniqueByWithCache(
                new NameValuePair<>(Wallet.Fields.type, type),
                new NameValuePair<>(Wallet.Fields.singleton, Boolean.TRUE)
        );
        assert exists(wallet) : "Should always find singleton wallets! type/" + type;
        return wallet;
    }

    public NrveValue getSumOfAllBalancesForType(WalletType walletType) {
        NrveValue value = getGSession().createNamedQuery("wallet.getSumOfAllBalancesForType", NrveValue.class)
                .setParameter("walletType", walletType)
                .uniqueResult();

        return value != null ? value : NrveValue.ZERO;
    }

    public Wallet getFirstWalletUsingNeoWallet(NeoWallet neoWallet) {
        return getFirstBy(new NameValuePair<>(Wallet.Fields.neoWallet, neoWallet));
    }

    public boolean isNeoWalletInUse(NeoWallet neoWallet) {
        return exists(getFirstWalletUsingNeoWallet(neoWallet));
    }
}
