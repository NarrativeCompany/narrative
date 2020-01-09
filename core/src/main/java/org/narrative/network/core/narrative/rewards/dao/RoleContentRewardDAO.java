package org.narrative.network.core.narrative.rewards.dao;

import org.narrative.common.persistence.OID;
import org.narrative.common.persistence.ObjectPair;
import org.narrative.common.persistence.ObjectTriplet;
import org.narrative.network.core.narrative.rewards.ContentCreatorRewardRole;
import org.narrative.network.core.narrative.rewards.RewardPeriod;
import org.narrative.network.core.narrative.rewards.RewardSlice;
import org.narrative.network.core.narrative.rewards.RoleContentReward;
import org.narrative.network.core.narrative.wallet.WalletTransaction;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Date: 2019-09-30
 * Time: 08:21
 *
 * @author brian
 */
public class RoleContentRewardDAO extends RewardTransactionRefDAO<RoleContentReward> {
    public RoleContentRewardDAO() {
        super(RoleContentReward.class);
    }

    public int insertRoleContentRewardsForPeriod(RewardPeriod rewardPeriod) {
        return getGSession().getNamedQuery("roleContentReward.insertRoleContentRewardsForPeriod")
                .setParameter("rewardPeriodOid", rewardPeriod.getOid())
                .setParameter("writerRole", ContentCreatorRewardRole.WRITER.getId())
                .executeUpdate();
    }

    public ObjectPair<Number,Number> getIncompleteCountAndTotalPointsForPeriod(RewardPeriod period) {
        return getGSession().createNamedQuery("roleContentReward.getIncompleteCountAndTotalPointsForPeriod", (Class<ObjectPair<Number,Number>>)(Class)ObjectPair.class)
                .setParameter("rewardPeriod", period)
                .setParameter("writerRole", ContentCreatorRewardRole.WRITER)
                .uniqueResult();
    }

    public List<RoleContentReward> getIncompleteRoleContentRewards(RewardPeriod period, int limit) {
        return getGSession().createNamedQuery("roleContentReward.getIncompleteRoleContentRewards", RoleContentReward.class)
                .setParameter("rewardPeriod", period)
                .setParameter("writerRole", ContentCreatorRewardRole.WRITER)
                .setMaxResults(limit)
                .list();
    }

    @Override
    public long getCountIncompleteRewardTransactionRefs(RewardPeriod period) {
        return getGSession().createNamedQuery("roleContentReward.getCountIncompleteRewardTransactionRefs", Number.class)
                .setParameter("rewardPeriod", period)
                .uniqueResult()
                .longValue();
    }

    @Override
    public long getCountInvalidRewardTransactions(RewardPeriod period) {
        return ((Number)getGSession().getNamedNativeQuery("roleContentReward.getCountInvalidRewardTransactions")
                .setParameter("fromWalletOid", period.getWallet().getOid())
                .setParameter("type", RewardSlice.CONTENT_CREATORS.getWalletTransactionType().getId())
                .uniqueResult())
                .longValue();
    }

    public Map<OID, ObjectPair<OID, ContentCreatorRewardRole>> getTransactionOidToRewardMetadata(Set<WalletTransaction> transactions) {
        List<ObjectTriplet<OID,OID,ContentCreatorRewardRole>> triplets = getGSession().createNamedQuery("roleContentReward.getTransactionOidAndRewardMetadata", (Class<ObjectTriplet<OID,OID,ContentCreatorRewardRole>>)(Class)ObjectTriplet.class)
                .setParameterList("transactions", transactions)
                .list();
        return ObjectTriplet.getAsMapOfOnesToTwoThreePairs(triplets);
    }
}
