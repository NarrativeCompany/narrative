package org.narrative.network.core.area.base.dao;

import org.narrative.common.persistence.OID;
import org.narrative.common.persistence.hibernate.HibernateUtil;
import org.narrative.network.core.area.base.ItemHourTrendingStats;
import org.narrative.network.shared.daobase.GlobalDAOImpl;
import org.hibernate.Criteria;
import org.hibernate.LockMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Property;
import org.hibernate.type.StandardBasicTypes;
import org.hibernate.type.Type;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Date: 4/26/13
 * Time: 4:21 PM
 * User: jonmark
 */
public class ItemHourTrendingStatsDAO extends GlobalDAOImpl<ItemHourTrendingStats, OID> {
    public ItemHourTrendingStatsDAO() {
        super(ItemHourTrendingStats.class);
    }

    public ItemHourTrendingStats getLockedForObjectOidAndHoursSinceEpoch(OID objectOid, long hoursSinceEpoch) {
        return (ItemHourTrendingStats) getGSession().getNamedQuery("itemHourTrendingStats.getLockedForObjectOidAndHoursSinceEpoch").setParameter("objectOid", objectOid).setParameter("hoursSinceEpoch", hoursSinceEpoch).setLockMode("ohs", LockMode.PESSIMISTIC_WRITE).uniqueResult();
    }

    public void deleteStaleObjects(long cutoffHours) {
        getGSession().getNamedQuery("itemHourTrendingStats.deleteStaleObjects").setParameter("cutoffHours", cutoffHours).executeUpdate();
    }

    private static final String ITEM_HOUR_TRENDING_STATS_ALIAS = "ihts";

    public static final int TRENDING_VIEW_MULTIPLIER = 1;
    public static final int TRENDING_REPLY_MULTIPLIER = 2;
    public static final int TRENDING_LIKE_MULTIPLIER = 4;

    private static final String TOTAL_VIEWS_FORMULA = newString("sum({alias}.", ItemHourTrendingStats.Fields.viewPoints, ")");
    private static final String TOTAL_REPLIES_FORMULA = newString("(sum({alias}.", ItemHourTrendingStats.Fields.replyPoints, ") * " + TRENDING_REPLY_MULTIPLIER + ")");
    private static final String TOTAL_LIKES_FORMULA = newString("(sum({alias}.", ItemHourTrendingStats.Fields.likePoints, ") * " + TRENDING_LIKE_MULTIPLIER + ")");

    private static final String ITEM_RANK_ALIAS = "itemRank";

    private static final String ITEM_RANK_PROJECTION = newString("(", TOTAL_VIEWS_FORMULA, " + ", TOTAL_REPLIES_FORMULA, " + ", TOTAL_LIKES_FORMULA, ") as ", ITEM_RANK_ALIAS);

    public Criteria getCriteria() {
        return ItemHourTrendingStats.dao().getPartitionType().currentSession().getSession().createCriteria(ItemHourTrendingStats.class, ITEM_HOUR_TRENDING_STATS_ALIAS);
    }

    public void addCriteriaProjection(Criteria entityCriteria, String entityOidField, Criteria statsCriteria) {
        statsCriteria.setProjection(Projections.projectionList().add(Property.forName(HibernateUtil.makeName(entityCriteria, entityOidField)).group()).add(Projections.alias(Projections.sqlProjection(ITEM_RANK_PROJECTION, new String[]{ITEM_RANK_ALIAS}, new Type[]{StandardBasicTypes.DOUBLE}), ITEM_RANK_ALIAS)));
        statsCriteria.addOrder(Order.desc(ITEM_RANK_ALIAS));
    }
}
