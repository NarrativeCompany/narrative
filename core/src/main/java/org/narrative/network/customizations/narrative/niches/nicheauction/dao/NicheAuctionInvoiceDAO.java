package org.narrative.network.customizations.narrative.niches.nicheauction.dao;

import org.narrative.common.persistence.OID;
import org.narrative.network.core.narrative.wallet.WalletTransaction;
import org.narrative.network.customizations.narrative.niches.niche.Niche;
import org.narrative.network.customizations.narrative.niches.nicheauction.NicheAuctionInvoice;
import org.narrative.network.shared.daobase.GlobalDAOImpl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: jonmark
 * Date: 2/22/18
 * Time: 9:38 AM
 */
public class NicheAuctionInvoiceDAO extends GlobalDAOImpl<NicheAuctionInvoice, OID> {
    public NicheAuctionInvoiceDAO() {
        super(NicheAuctionInvoice.class);
    }

    public Map<OID, Niche> getRefundTransactionOidToNiche(Set<WalletTransaction> transactions) {
        List<Object[]> objs = getGSession().getNamedQuery("nicheAuctionInvoice.getRefundTransactionOidToNiche")
                .setParameterList("transactions", transactions)
                .list();
        Map<OID,Niche> transactionOidToNiche = new HashMap<>();
        for (Object[] obj : objs) {
            transactionOidToNiche.put((OID)obj[0], (Niche)obj[1]);
        }
        return transactionOidToNiche;
    }

    public Map<OID, Niche> getReversalTransactionOidToNiche(Set<WalletTransaction> transactions) {
        List<Object[]> objs = getGSession().getNamedQuery("nicheAuctionInvoice.getReversalTransactionOidToNiche")
                .setParameterList("transactions", transactions)
                .list();
        Map<OID,Niche> transactionOidToNiche = new HashMap<>();
        for (Object[] obj : objs) {
            transactionOidToNiche.put((OID)obj[0], (Niche)obj[1]);
        }
        return transactionOidToNiche;
    }
}
