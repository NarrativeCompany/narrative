package org.narrative.network.core.narrative.wallet;

import org.narrative.common.persistence.DAO;
import org.narrative.common.persistence.DAOObject;

/**
 * Date: 2019-05-15
 * Time: 20:53
 *
 * @author jonmark
 */
public interface WalletTransactionRef<T extends DAO> extends DAOObject<T> {
    String FIELD__TRANSACTION__NAME = "transaction";
    String FIELD__TRANSACTION__COLUMN = FIELD__TRANSACTION__NAME+"_"+ WalletTransaction.FIELD__OID__NAME;

    WalletTransaction getTransaction();
    void setTransaction(WalletTransaction transaction);
}
