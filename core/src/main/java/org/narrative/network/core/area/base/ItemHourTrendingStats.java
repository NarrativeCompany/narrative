package org.narrative.network.core.area.base;

import org.narrative.common.persistence.DAOImpl;
import org.narrative.common.persistence.DAOObject;
import org.narrative.common.persistence.OID;
import org.narrative.common.persistence.OIDGenerator;
import org.narrative.common.persistence.hibernate.HibernateUtil;
import org.narrative.common.util.IPDateUtil;
import org.narrative.network.core.area.base.dao.ItemHourTrendingStatsDAO;
import org.narrative.network.core.content.base.Content;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.Proxy;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;

/**
 * Date: 4/26/13
 * Time: 4:21 PM
 * User: jonmark
 */
@Getter
@Setter
@Entity
@Proxy
@FieldNameConstants
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {ItemHourTrendingStats.COLUMN__OBJECT_OID, ItemHourTrendingStats.COLUMN__HOURS_SINCE_EPOCH}))
public class ItemHourTrendingStats implements DAOObject<ItemHourTrendingStatsDAO> {
    public static final String FIELD__OBJECT_OID = "objectOid";
    public static final String COLUMN__OBJECT_OID = FIELD__OBJECT_OID;
    public static final String FIELD__HOURS_SINCE_EPOCH = "hoursSinceEpoch";
    public static final String COLUMN__HOURS_SINCE_EPOCH = FIELD__HOURS_SINCE_EPOCH;

    /**
     * trending only applies to the past 5 days of activity
     */
    public static final long CUTOFF_IN_DAYS = 5L;
    public static final long CUTOFF_IN_HOURS = CUTOFF_IN_DAYS * IPDateUtil.DAY_IN_HOURS;

    /**
     * we'll keep 100 days of history for auditing purposes. 100 days guarantees we will have at least 3 months'
     * worth of statistics. needed for Narrative Rewards.
     */
    private static final long HISTORY_IN_DAYS = 100L;
    public static final long HISTORY_IN_HOURS = HISTORY_IN_DAYS * IPDateUtil.DAY_IN_HOURS;
    public static final long HISTORY_IN_MS = HISTORY_IN_DAYS * IPDateUtil.DAY_IN_MS;

    @Id
    @GeneratedValue(generator = OIDGenerator.NAME)
    private OID oid;
    @NotNull
    private OID objectOid;

    private long hoursSinceEpoch;

    private long viewPoints = 0;
    private long replyPoints = 0;
    private long likePoints = 0;

    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = Fields.objectOid, insertable = false, updatable = false)
    @ForeignKey(name = HibernateUtil.NO_FOREIGN_KEY_NAME)
    private Content content;

    @Deprecated
    public ItemHourTrendingStats() {}

    public ItemHourTrendingStats(Content content) {
        this(content.getOid(), null);
    }

    public ItemHourTrendingStats(Content content, long msOfEvent) {
        this(content.getOid(), msOfEvent);
    }

    private ItemHourTrendingStats(OID objectOid, Long msOfEvent) {
        this.objectOid = objectOid;
        this.hoursSinceEpoch = getHoursSinceTheEpoch(msOfEvent);
    }

    public static long getHoursSinceTheEpoch(long millis) {
        return millis / IPDateUtil.HOUR_IN_MS;
    }

    public static long getHoursSinceTheEpoch() {
        return getHoursSinceTheEpoch(System.currentTimeMillis());
    }

    private  static long getHoursSinceTheEpoch(Long millis) {
        return getHoursSinceTheEpoch(millis != null ? millis : System.currentTimeMillis());
    }

    public static ItemHourTrendingStatsDAO dao() {
        return DAOImpl.getDAO(ItemHourTrendingStats.class);
    }
}
