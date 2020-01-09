package org.narrative.network.core.narrative.wallet.dao;

import org.narrative.common.persistence.NameValuePair;
import org.narrative.common.persistence.OID;
import org.narrative.network.core.narrative.wallet.NeoWallet;
import org.narrative.network.core.narrative.wallet.NeoWalletType;
import org.narrative.network.customizations.narrative.neo.services.NeoUtils;
import org.narrative.network.shared.daobase.GlobalDAOImpl;

import java.util.Collection;
import java.util.List;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Date: 2019-06-11
 * Time: 10:35
 *
 * @author brian
 */
public class NeoWalletDAO extends GlobalDAOImpl<NeoWallet, OID> {
    public NeoWalletDAO() {
        super(NeoWallet.class);
    }

    public List<NeoWallet> getAllByTypes(Collection<NeoWalletType> types) {
        return getGSession().createNamedQuery("neoWallet.getAllByTypes", NeoWallet.class)
                .setParameterList("types", types)
                .list();
    }

    public String getNichePaymentNeoAddress() {
        return getSingletonWallet(NeoWalletType.NICHE_PAYMENT).getNeoAddress();
    }

    public String getPublicationPaymentNeoAddress() {
        return getSingletonWallet(NeoWalletType.PUBLICATION_PAYMENT).getNeoAddress();
    }

    public NeoWallet getSingletonWallet(NeoWalletType type) {
        assert type.isSingleton() : "Should only use singleton types with this method! not type/" + type;
        NeoWallet neoWallet = getUniqueByWithCache(
                new NameValuePair<>(NeoWallet.Fields.type, type),
                new NameValuePair<>(NeoWallet.Fields.singleton, Boolean.TRUE)
        );
        assert exists(neoWallet) : "Should always find singleton wallets! type/" + type;
        return neoWallet;
    }

    public NeoWallet getForNeoAddress(String neoAddress) {
        assert NeoUtils.isValidAddress(neoAddress) : "This method should only be used for valid neo addresses.";

        return getUniqueBy(new NameValuePair<>(NeoWallet.Fields.neoAddress, neoAddress));
    }
}
