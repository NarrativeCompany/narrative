package org.narrative.network.core.narrative.rewards.dao;

import org.narrative.common.persistence.OID;
import org.narrative.common.persistence.ObjectPair;
import org.narrative.network.core.narrative.rewards.NicheContentReward;
import org.narrative.network.core.narrative.rewards.NicheReward;
import org.narrative.network.core.narrative.rewards.RewardPeriod;
import org.narrative.network.customizations.narrative.NrveValue;
import org.narrative.network.customizations.narrative.niches.niche.Niche;
import org.narrative.network.shared.daobase.GlobalDAOImpl;

import java.util.ArrayList;
import java.util.List;

/**
 * Date: 2019-05-16
 * Time: 08:25
 *
 * @author jonmark
 */
public class NicheContentRewardDAO extends GlobalDAOImpl<NicheContentReward, OID> {
    public NicheContentRewardDAO() {
        super(NicheContentReward.class);
    }

    public int insertNicheContentRewardsForPeriod(RewardPeriod rewardPeriod) {
        return getGSession().getNamedQuery("nicheContentReward.insertNicheContentRewardsForPeriod")
                .setParameter("rewardPeriodOid", rewardPeriod.getOid())
                .executeUpdate();
    }

    public int clearNicheContentRewardValuesForPeriod(RewardPeriod rewardPeriod) {
        return getGSession().getNamedQuery("nicheContentReward.clearNicheContentRewardValuesForPeriod")
                .setParameter("rewardPeriodOid", rewardPeriod.getOid())
                .executeUpdate();
    }

    public long getTotalPointsForPeriod(RewardPeriod period) {
        Number val = getGSession().createNamedQuery("nicheContentReward.getTotalPointsForPeriod", Number.class)
                .setParameter("rewardPeriod", period)
                .uniqueResult();
        return val==null ? 0 : val.longValue();
    }

    public long getIncompleteNicheCountForPeriod(RewardPeriod period) {
        return getGSession().createNamedQuery("nicheContentReward.getIncompleteNicheCountForPeriod", Number.class)
                .setParameter("rewardPeriod", period)
                .uniqueResult()
                .longValue();
    }

    public ObjectPair<Number,Number> getIncompleteContentCountAndTotalPointsForNicheReward(NicheReward nicheReward) {
        return getGSession().createNamedQuery("nicheContentReward.getIncompleteContentCountAndTotalPointsForNicheReward", (Class<ObjectPair<Number,Number>>)(Class)ObjectPair.class)
                .setParameter("nicheReward", nicheReward)
                .uniqueResult();
    }

    public List<NicheContentReward> getIncompleteForNicheReward(NicheReward nicheReward, int limit) {
        return getGSession().createNamedQuery("nicheContentReward.getIncompleteForNicheReward", NicheContentReward.class)
                .setParameter("nicheReward", nicheReward)
                .setMaxResults(limit)
                .list();
    }

    public long getCountForNicheReward(NicheReward nicheReward) {
        return getGSession().createNamedQuery("nicheContentReward.getCountForNicheReward", Number.class)
                .setParameter("nicheReward", nicheReward)
                .uniqueResult()
                .longValue();
    }

    public NrveValue getRewardTotalForNicheReward(NicheReward nicheReward) {
        NrveValue value = getGSession().createNamedQuery("nicheContentReward.getRewardTotalForNicheReward", NrveValue.class)
                .setParameter("nicheReward", nicheReward)
                .uniqueResult();

        return value != null ? value : NrveValue.ZERO;
    }

    public List<ObjectPair<OID,NrveValue>> getTopPostsForNicheReward(NicheReward nicheReward, int limit) {
        List<Object[]> objs = getGSession().getNamedQuery("nicheContentReward.getTopPostsForNicheReward")
                .setParameter("nicheReward", nicheReward)
                .setMaxResults(limit)
                .list();

        return getTopItemsList(objs);
    }

    public List<ObjectPair<OID,NrveValue>> getTopPostsAllTimeForNiche(Niche niche, int limit) {
        List<Object[]> objs = getGSession().getNamedQuery("nicheContentReward.getTopPostsAllTimeForNiche")
                .setParameter("niche", niche)
                .setMaxResults(limit)
                .list();

        return getTopItemsList(objs);
    }

    private List<ObjectPair<OID,NrveValue>> getTopItemsList(List<Object[]> objs) {
        List<ObjectPair<OID,NrveValue>> ret = new ArrayList<>(objs.size());
        for (Object[] obj : objs) {
            ret.add(new ObjectPair<>((OID)obj[0], new NrveValue(((Number)obj[1]).longValue())));
        }
        return ret;
    }

    public List<ObjectPair<OID, NrveValue>> getTopCreatorsForNicheReward(NicheReward nicheReward, int limit) {
        List<Object[]> objs = getGSession().getNamedQuery("nicheContentReward.getTopCreatorsForNicheReward")
                .setParameter("nicheReward", nicheReward)
                .setMaxResults(limit)
                .list();

        return getTopItemsList(objs);
    }

    public List<ObjectPair<OID, NrveValue>> getTopCreatorsAllTimeForNiche(Niche niche, int limit) {
        List<Object[]> objs = getGSession().getNamedQuery("nicheContentReward.getTopCreatorsAllTimeForNiche")
                .setParameter("niche", niche)
                .setMaxResults(limit)
                .list();

        return getTopItemsList(objs);
    }
}
