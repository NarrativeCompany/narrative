package org.narrative.network.core.narrative.rewards.dao;

import org.narrative.common.persistence.NameValuePair;
import org.narrative.common.persistence.OID;
import org.narrative.common.persistence.ObjectPair;
import org.narrative.network.core.narrative.rewards.NicheOwnerReward;
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
 * Date: 2019-05-15
 * Time: 21:09
 *
 * @author jonmark
 */
public class NicheOwnerRewardDAO extends RewardTransactionRefDAO<NicheOwnerReward> {
    public NicheOwnerRewardDAO() {
        super(NicheOwnerReward.class);
    }

    public NicheOwnerReward getForNicheReward(NicheReward nicheReward) {
        return getUniqueBy(new NameValuePair<>(NicheOwnerReward.Fields.nicheReward, nicheReward));
    }

    public int insertNicheOwnerRewardsForPeriod(RewardPeriod rewardPeriod) {
        return getGSession().getNamedQuery("nicheOwnerReward.insertNicheOwnerRewardsForPeriod")
                .setParameter("rewardPeriodOid", rewardPeriod.getOid())
                .setParameter("deletedUserStatus", UserStatus.DELETED.getBitmask())
                .executeUpdate();
    }

    @Override
    public long getCountIncompleteRewardTransactionRefs(RewardPeriod period) {
        return ((Number)getGSession().getNamedNativeQuery("nicheOwnerReward.getCountIncompleteRewardTransactionRefs")
                .setParameter("rewardPeriodOid", period.getOid())
                .uniqueResult())
                .longValue();
    }

    @Override
    public long getCountInvalidRewardTransactions(RewardPeriod period) {
        return ((Number)getGSession().getNamedNativeQuery("nicheOwnerReward.getCountInvalidRewardTransactions")
                .setParameter("fromWalletOid", period.getWallet().getOid())
                .setParameter("type", RewardSlice.NICHE_OWNERS.getWalletTransactionType().getId())
                .uniqueResult())
                .longValue();
    }

    public long getIncompleteCountForPeriod(RewardPeriod period) {
        return getGSession().createNamedQuery("nicheOwnerReward.getIncompleteCountForPeriod", Number.class)
                .setParameter("rewardPeriod", period)
                .uniqueResult()
                .longValue();
    }

    public List<ObjectPair<NicheOwnerReward,Long>> getIncompleteNicheOwnerRewards(RewardPeriod period, int limit) {
        List<Object[]> list = getGSession().getNamedQuery("nicheOwnerReward.getIncompleteNicheOwnerRewards")
                .setParameter("rewardPeriod", period)
                .setMaxResults(limit)
                .list();

        List<ObjectPair<NicheOwnerReward,Long>> ret = new ArrayList<>(list.size());
        for (Object[] objs : list) {
            ret.add(new ObjectPair<>((NicheOwnerReward)objs[0], ((Number)objs[1]).longValue()));
        }
        return ret;
    }

    public NicheOwnerReward getNicheOwnerRewardForContentProcessing(RewardPeriod period) {
        return getGSession().createNamedQuery("nicheOwnerReward.getNicheOwnerRewardForContentProcessing", NicheOwnerReward.class)
                .setParameter("rewardPeriod", period)
                .setMaxResults(1)
                .uniqueResult();
    }

    public NrveValue getNicheAllTimeRewards(Niche niche) {
        NrveValue value = getGSession().createNamedQuery("nicheOwnerReward.getNicheAllTimeRewards", NrveValue.class)
                .setParameter("niche", niche)
                .uniqueResult();
        return value != null ? value : NrveValue.ZERO;
    }

    public List<NicheOwnerReward> getForUserRewardPeriod(User user, RewardPeriod rewardPeriod) {
        return getGSession().createNamedQuery("nicheOwnerReward.getForUserRewardPeriod", NicheOwnerReward.class)
                .setParameter("user", user)
                .setParameter("rewardPeriod", rewardPeriod)
                .list();
    }

    public Map<OID, Niche> getTransactionOidToNiche(Set<WalletTransaction> transactions) {
        List<Object[]> objs = getGSession().getNamedQuery("nicheOwnerReward.getTransactionOidToNiche")
                .setParameterList("transactions", transactions)
                .list();
        Map<OID,Niche> ret = new HashMap<>();
        for (Object[] obj : objs) {
            ret.put((OID)obj[0], (Niche)obj[1]);
        }
        return ret;
    }
}
