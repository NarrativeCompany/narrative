package org.narrative.network.core.narrative.rewards.dao;

import org.narrative.common.persistence.OID;
import org.narrative.network.core.area.base.ItemHourTrendingStats;
import org.narrative.network.core.area.base.dao.ItemHourTrendingStatsDAO;
import org.narrative.network.core.content.base.Content;
import org.narrative.network.core.narrative.rewards.ContentReward;
import org.narrative.network.core.narrative.rewards.RewardPeriod;
import org.narrative.network.customizations.narrative.NrveValue;
import org.narrative.network.customizations.narrative.channels.ChannelType;
import org.narrative.network.customizations.narrative.niches.niche.NicheStatus;
import org.narrative.network.customizations.narrative.posts.NarrativePostStatus;
import org.narrative.network.customizations.narrative.posts.QualityLevel;
import org.narrative.network.customizations.narrative.publications.PublicationStatus;
import org.narrative.network.shared.daobase.GlobalDAOImpl;

/**
 * Date: 2019-05-16
 * Time: 08:16
 *
 * @author jonmark
 */
public class ContentRewardDAO extends GlobalDAOImpl<ContentReward, OID> {
    public ContentRewardDAO() {
        super(ContentReward.class);
    }

    public int createTemporaryNicheContentTable() {
        return getGSession().getNamedQuery("contentReward.createTemporaryNicheContentTable")
                .setParameter("approvedPostStatus", NarrativePostStatus.APPROVED.getId())
                .setParameter("nicheChannel", ChannelType.NICHE.getId())
                .setParameter("activeNicheStatus", NicheStatus.ACTIVE.getId())
                .setParameter("minScore", QualityLevel.MEDIUM.getMinimumScore())
                .executeUpdate();
    }

    public int dropTemporaryNicheContentTable() {
        return getGSession().getNamedQuery("contentReward.dropTemporaryNicheContentTable")
                .executeUpdate();
    }

    public int createTemporaryPublicationContentTable() {
        return getGSession().getNamedQuery("contentReward.createTemporaryPublicationContentTable")
                .setParameter("approvedPostStatus", NarrativePostStatus.APPROVED.getId())
                .setParameter("publicationChannel", ChannelType.PUBLICATION.getId())
                .setParameter("activePublicationStatus", PublicationStatus.ACTIVE.getId())
                .setParameter("minScore", QualityLevel.MEDIUM.getMinimumScore())
                .executeUpdate();
    }

    public int dropTemporaryPublicationContentTable() {
        return getGSession().getNamedQuery("contentReward.dropTemporaryPublicationContentTable")
                .executeUpdate();
    }

    public int insertContentRewardsForPeriod(RewardPeriod rewardPeriod) {
        return getGSession().getNamedQuery("contentReward.insertContentRewardsForPeriod")
                .setParameter("viewMultiplier", ItemHourTrendingStatsDAO.TRENDING_VIEW_MULTIPLIER)
                .setParameter("replyMultiplier", ItemHourTrendingStatsDAO.TRENDING_REPLY_MULTIPLIER)
                .setParameter("likeMultiplier", ItemHourTrendingStatsDAO.TRENDING_LIKE_MULTIPLIER)
                .setParameter("rewardPeriodOid", rewardPeriod.getOid())
                .setParameter("lowerBound", ItemHourTrendingStats.getHoursSinceTheEpoch(rewardPeriod.getRewardYearMonth().getLowerBoundForQuery().toEpochMilli()))
                .setParameter("upperBound", ItemHourTrendingStats.getHoursSinceTheEpoch(rewardPeriod.getRewardYearMonth().getUpperBoundForQuery().toEpochMilli()))
                .executeUpdate();
    }

    public NrveValue getAllTimeRewardsForContent(Content content) {
        NrveValue value = getGSession().createNamedQuery("contentReward.getAllTimeRewardsForContent", NrveValue.class)
                .setParameter("contentOid", content.getOid())
                .uniqueResult();
        return value != null ? value : NrveValue.ZERO;
    }
}
