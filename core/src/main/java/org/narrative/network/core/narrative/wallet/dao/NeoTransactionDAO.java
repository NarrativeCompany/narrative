package org.narrative.network.core.narrative.wallet.dao;

import org.narrative.common.persistence.OID;
import org.narrative.network.core.narrative.wallet.NeoTransaction;
import org.narrative.network.core.narrative.wallet.NeoTransactionType;
import org.narrative.network.core.narrative.wallet.NeoWallet;
import org.narrative.network.shared.daobase.GlobalDAOImpl;

import java.util.Collection;
import java.util.List;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Date: 2019-06-11
 * Time: 10:40
 *
 * @author brian
 */
public class NeoTransactionDAO extends GlobalDAOImpl<NeoTransaction, OID> {
    public NeoTransactionDAO() {
        super(NeoTransaction.class);
    }

    public List<NeoTransaction> getAllIncomplete(Collection<NeoTransactionType> types) {
        return getGSession().createNamedQuery("neoTransaction.getAllIncomplete", NeoTransaction.class)
                .setParameterList("types", types)
                .list();
    }

    public List<NeoTransaction> getAllCompleted(int page, int rowsPerPage) {
        // bl: Hibernate returns these to us as BigIntegers
        List<Number> oids = getGSession().getNamedNativeQuery("neoTransaction.getAllCompleted")
                .setMaxResults(rowsPerPage)
                .setFirstResult((page-1)*rowsPerPage)
                .list();
        return getObjectsFromIDs(OID.getOIDsFromCollection(oids));
    }

    public int getCountCompleted() {
        return getGSession().createNamedQuery("neoTransaction.getCountCompleted", Number.class)
                .uniqueResult().intValue();
    }

    public NeoTransaction getFirstForWallet(NeoWallet neoWallet) {
        return getGSession().createNamedQuery("neoTransaction.getFirstForWallet", NeoTransaction.class)
                .setParameter("neoWallet", neoWallet)
                .setMaxResults(1)
                .uniqueResult();
    }

    public boolean isDoesWalletHaveTransactions(NeoWallet neoWallet) {
        return exists(getFirstForWallet(neoWallet));
    }
}
