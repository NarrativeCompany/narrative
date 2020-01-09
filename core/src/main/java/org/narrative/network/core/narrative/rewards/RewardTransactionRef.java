package org.narrative.network.core.narrative.rewards;

import org.narrative.common.persistence.DAO;
import org.narrative.network.core.narrative.wallet.WalletTransaction;
import org.narrative.network.core.narrative.wallet.WalletTransactionType;
import org.narrative.network.core.narrative.wallet.WalletTransactionRef;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Date: 2019-05-16
 * Time: 08:01
 *
 * @author jonmark
 */
public interface RewardTransactionRef<T extends DAO> extends WalletTransactionRef<T> {

    WalletTransactionType getExpectedTransactionType();

    default void updateTransaction(WalletTransaction transaction) {
        assert exists(transaction) : "Transaction should always be provided to this method.";
        assert isEqual(getExpectedTransactionType(), transaction.getType()) : "Expected "+getExpectedTransactionType()+" transaction, but got "+transaction.getType();

        setTransaction(transaction);
    }
}
