package org.narrative.network.core.narrative.rewards.dao;

import org.narrative.common.persistence.OID;
import org.narrative.common.persistence.ObjectPair;
import org.narrative.network.core.narrative.rewards.NicheModeratorReward;
import org.narrative.network.core.narrative.rewards.NicheReward;
import org.narrative.network.core.narrative.rewards.RewardPeriod;
import org.narrative.network.core.narrative.rewards.RewardSlice;
import org.narrative.network.core.narrative.wallet.WalletTransaction;
import org.narrative.network.core.user.User;
import org.narrative.network.core.user.UserStatus;
import org.narrative.network.customizations.narrative.NrveValue;
import org.narrative.network.customizations.narrative.niches.niche.Niche;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Date: 2019-05-16
 * Time: 07:34
 *
 * @author jonmark
 */
public class NicheModeratorRewardDAO extends RewardTransactionRefDAO<NicheModeratorReward> {
    public NicheModeratorRewardDAO() {
        super(NicheModeratorReward.class);
    }

    public int insertNicheModeratorRewardsForPeriod(RewardPeriod rewardPeriod) {
        return getGSession().getNamedQuery("nicheModeratorReward.insertNicheModeratorRewardsForPeriod")
                .setParameter("rewardPeriodOid", rewardPeriod.getOid())
                .setParameter("deletedUserStatus", UserStatus.DELETED.getBitmask())
                .executeUpdate();
    }

    @Override
    public long getCountIncompleteRewardTransactionRefs(RewardPeriod period) {
        return getGSession().createNamedQuery("nicheModeratorReward.getCountIncompleteRewardTransactionRefs", Number.class)
                .setParameter("rewardPeriod", period)
                .uniqueResult()
                .longValue();
    }

    @Override
    public long getCountInvalidRewardTransactions(RewardPeriod period) {
        return ((Number)getGSession().getNamedNativeQuery("nicheModeratorReward.getCountInvalidRewardTransactions")
                .setParameter("fromWalletOid", period.getWallet().getOid())
                .setParameter("type", RewardSlice.NICHE_MODERATORS.getWalletTransactionType().getId())
                .uniqueResult())
                .longValue();
    }

    public long getIncompleteNicheCountForPeriod(RewardPeriod period) {
        return getGSession().createNamedQuery("nicheModeratorReward.getIncompleteNicheCountForPeriod", Number.class)
                .setParameter("rewardPeriod", period)
                .uniqueResult()
                .longValue();
    }

    public List<ObjectPair<OID,Long>> getIncompleteNicheModeratorRewards(RewardPeriod period, int limit) {
        List<Object[]> list = getGSession().getNamedQuery("nicheModeratorReward.getIncompleteNicheModeratorRewards")
                .setParameter("rewardPeriod", period)
                .setMaxResults(limit)
                .list();

        List<ObjectPair<OID,Long>> ret = new ArrayList<>(list.size());
        for (Object[] objs : list) {
            ret.add(new ObjectPair<>((OID)objs[0], ((Number)objs[1]).longValue()));
        }
        return ret;
    }

    public NrveValue getTransactionSumForNicheReward(NicheReward nicheReward) {
        NrveValue value = getGSession().createNamedQuery("nicheModeratorReward.getTransactionSumForNicheReward", NrveValue.class)
                .setParameter("nicheReward", nicheReward)
                .uniqueResult();
        return value != null ? value : NrveValue.ZERO;
    }

    public NrveValue getTransactionSumForUserRewardPeriod(User user, RewardPeriod rewardPeriod) {
        NrveValue value = getGSession().createNamedQuery("nicheModeratorReward.getTransactionSumForUserRewardPeriod", NrveValue.class)
                .setParameter("user", user)
                .setParameter("rewardPeriod", rewardPeriod)
                .uniqueResult();
        return value != null ? value : NrveValue.ZERO;
    }

    public Map<OID, Niche> getTransactionOidToNiche(Set<WalletTransaction> transactions) {
        List<Object[]> objs = getGSession().getNamedQuery("nicheModeratorReward.getTransactionOidToNiche")
                .setParameterList("transactions", transactions)
                .list();
        Map<OID,Niche> ret = new HashMap<>();
        for (Object[] obj : objs) {
            ret.put((OID)obj[0], (Niche)obj[1]);
        }
        return ret;
    }
}
