package org.narrative.network.core.narrative.rewards.dao;

import org.narrative.common.persistence.NameValuePair;
import org.narrative.common.persistence.OID;
import org.narrative.network.core.narrative.rewards.NicheReward;
import org.narrative.network.core.narrative.rewards.RewardPeriod;
import org.narrative.network.customizations.narrative.niches.niche.Niche;
import org.narrative.network.shared.daobase.GlobalDAOImpl;

import java.util.List;

/**
 * Date: 2019-05-13
 * Time: 14:51
 *
 * @author jonmark
 */
public class NicheRewardDAO extends GlobalDAOImpl<NicheReward, OID> {
    public NicheRewardDAO() {
        super(NicheReward.class);
    }

    public int insertNicheRewardsForPeriod(RewardPeriod rewardPeriod) {
        return getGSession().getNamedQuery("nicheReward.insertNicheRewardsForPeriod")
                .setParameter("rewardPeriodOid", rewardPeriod.getOid())
                .executeUpdate();
    }

    public NicheReward getForNichePeriod(Niche niche, RewardPeriod rewardPeriod) {
        return getUniqueBy(new NameValuePair<>(NicheReward.Fields.niche, niche), new NameValuePair<>(NicheReward.Fields.period, rewardPeriod));
    }

    public List<RewardPeriod> getNicheRewardPeriods(Niche niche) {
        return getGSession().createNamedQuery("nicheReward.getNicheRewardPeriods", RewardPeriod.class)
                .setParameter("niche", niche)
                .list();
    }
}
