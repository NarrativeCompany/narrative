package org.narrative.network.core.narrative.wallet.dao;

import org.narrative.common.persistence.OID;
import org.narrative.network.core.narrative.wallet.NeoTransactionId;
import org.narrative.network.shared.daobase.GlobalDAOImpl;

/**
 * Date: 2019-06-14
 * Time: 11:19
 *
 * @author brian
 */
public class NeoTransactionIdDAO extends GlobalDAOImpl<NeoTransactionId, OID> {
    public NeoTransactionIdDAO() {
        super(NeoTransactionId.class);
    }
}
