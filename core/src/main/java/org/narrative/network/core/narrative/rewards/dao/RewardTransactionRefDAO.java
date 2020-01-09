package org.narrative.network.core.narrative.rewards.dao;

import org.narrative.common.persistence.OID;
import org.narrative.network.core.narrative.rewards.RewardPeriod;
import org.narrative.network.core.narrative.rewards.RewardTransactionRef;
import org.narrative.network.shared.daobase.GlobalDAOImpl;
import org.jetbrains.annotations.NotNull;

/**
 * Date: 2019-05-30
 * Time: 08:10
 *
 * @author brian
 */
public abstract class RewardTransactionRefDAO<T extends RewardTransactionRef> extends GlobalDAOImpl<T,OID> {
    public RewardTransactionRefDAO(@NotNull Class<T> cls) {
        super(cls);
    }

    public abstract long getCountIncompleteRewardTransactionRefs(RewardPeriod period);

    public abstract long getCountInvalidRewardTransactions(RewardPeriod period);
}
