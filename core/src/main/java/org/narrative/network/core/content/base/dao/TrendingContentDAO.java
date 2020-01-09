package org.narrative.network.core.content.base.dao;

import org.narrative.common.persistence.NameValuePair;
import org.narrative.common.persistence.OID;
import org.narrative.network.core.area.base.ItemHourTrendingStats;
import org.narrative.network.core.area.base.dao.ItemHourTrendingStatsDAO;
import org.narrative.network.core.content.base.Content;
import org.narrative.network.core.content.base.TrendingContent;
import org.narrative.network.core.settings.global.GlobalSettings;
import org.narrative.network.core.settings.global.services.GlobalSettingsUtil;
import org.narrative.network.shared.daobase.GlobalDAOImpl;

import java.time.Instant;
import java.util.List;

/**
 * Date: 2019-02-24
 * Time: 11:39
 *
 * @author jonmark
 */
public class TrendingContentDAO extends GlobalDAOImpl<TrendingContent, OID> {
    public TrendingContentDAO() {
        super(TrendingContent.class);
    }

    public int calculateTrendingContent(Instant buildTime) {
        return getGSession().getNamedQuery("trendingContent.calculateTrendingContent")
                // jw: since this is an sql query, we need to do the same conversation that the HibernateInstantType does.
                .setParameter("buildTime", buildTime.toEpochMilli())
                .setParameter("viewsMultiplier", ItemHourTrendingStatsDAO.TRENDING_VIEW_MULTIPLIER)
                .setParameter("repliesMultiplier", ItemHourTrendingStatsDAO.TRENDING_REPLY_MULTIPLIER)
                .setParameter("likePointsMultiplier", ItemHourTrendingStatsDAO.TRENDING_LIKE_MULTIPLIER)
                .setParameter("minimumHoursSinceEpoch", ItemHourTrendingStats.getHoursSinceTheEpoch() - ItemHourTrendingStats.CUTOFF_IN_HOURS)
                .setParameter("hoursInRange", ItemHourTrendingStats.CUTOFF_IN_HOURS)
                .executeUpdate();
    }

    public int deleteOldTrendingContent(Instant buildTime) {
        return getGSession().getNamedQuery("trendingContent.deleteOldTrendingContent")
                .setParameter("buildTime", buildTime)
                .executeUpdate();
    }

    public TrendingContent getCurrentTrendingContent(Content content) {
        GlobalSettings globalSettings = GlobalSettingsUtil.getGlobalSettings();
        return getUniqueByWithCache(new NameValuePair<>(TrendingContent.FIELD__CONTENT, content), new NameValuePair<>(TrendingContent.FIELD__BUILD_TIME, globalSettings.getCurrentTrendingContentBuildTime()));
    }

    public List<OID> getTrendingNicheOids(int count) {
        GlobalSettings globalSettings = GlobalSettingsUtil.getGlobalSettings();
        return getGSession().getNamedQuery("trendingContent.getTrendingNicheOids")
                .setParameter("buildTime", globalSettings.getCurrentTrendingContentBuildTime())
                .setMaxResults(count)
                .list();
    }
}
