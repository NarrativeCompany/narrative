package org.narrative.network.core.narrative.rewards.dao;

import org.narrative.common.persistence.OID;
import org.narrative.network.core.narrative.rewards.PublicationReward;
import org.narrative.network.core.narrative.rewards.RewardPeriod;
import org.narrative.network.customizations.narrative.publications.PublicationStatus;
import org.narrative.network.shared.daobase.GlobalDAOImpl;

/**
 * Date: 2019-09-30
 * Time: 11:47
 *
 * @author brian
 */
public class PublicationRewardDAO extends GlobalDAOImpl<PublicationReward, OID> {
    public PublicationRewardDAO() {
        super(PublicationReward.class);
    }

    public int insertPublicationRewardsForPeriod(RewardPeriod rewardPeriod) {
        return getGSession().getNamedQuery("publicationReward.insertPublicationRewardsForPeriod")
                .setParameter("rewardPeriodOid", rewardPeriod.getOid())
                .setParameter("activeStatus", PublicationStatus.ACTIVE.getId())
                .executeUpdate();
    }
}
