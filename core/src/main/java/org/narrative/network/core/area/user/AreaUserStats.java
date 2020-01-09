package org.narrative.network.core.area.user;

import org.narrative.common.persistence.DAOImpl;
import org.narrative.common.persistence.DAOObject;
import org.narrative.common.persistence.OID;
import org.narrative.common.persistence.hibernate.HibernateUtil;
import org.narrative.network.core.area.user.dao.AreaUserStatsDAO;
import org.narrative.network.core.composition.base.CompositionConsumer;
import org.narrative.network.core.composition.base.CompositionConsumerType;
import org.narrative.network.core.composition.base.Reply;
import org.narrative.network.core.content.base.CompositionConsumerCounts;
import org.narrative.network.core.system.NetworkRegistry;
import org.apache.commons.lang3.time.DateUtils;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Proxy;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.MapsId;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

import java.sql.Timestamp;

/**
 * Created by IntelliJ IDEA.
 * User: paul
 * Date: Nov 23, 2005
 * Time: 3:51:40 PM
 */
@Entity
@Proxy
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class AreaUserStats implements DAOObject<AreaUserStatsDAO> {
    private OID oid;
    private AreaUser areaUser;
    private CompositionConsumerCounts cumulativeContentCounts;
    private CompositionConsumerCounts cumulativeReplyCounts;
    private Timestamp lastLoginDatetime;
    private Timestamp previousLoginDatetime;

    public static final String FIELD__AREA_USER__NAME = "areaUser";
    public static final String FIELD__LAST_LOGIN_DATETIME__NAME = "lastLoginDatetime";

    /**
     * @deprecated for hibernate use only
     */
    public AreaUserStats() {}

    public AreaUserStats(AreaUser areaUser) {
        this.areaUser = areaUser;
        this.cumulativeContentCounts = new CompositionConsumerCounts();
        this.cumulativeReplyCounts = new CompositionConsumerCounts();
    }

    @Id
    @GeneratedValue(generator = HibernateUtil.FOREIGN_GENERIC_GENERATOR_NAME)
    @GenericGenerator(name = HibernateUtil.FOREIGN_GENERIC_GENERATOR_NAME, strategy = HibernateUtil.FOREIGN_STRATEGY, parameters = {@Parameter(name = HibernateUtil.FOREIGN_STRATEGY_PROPERTY_NAME, value = FIELD__AREA_USER__NAME)})
    public OID getOid() {
        return oid;
    }

    public void setOid(OID oid) {
        this.oid = oid;
    }

    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = AreaUserStats.FIELD__OID__NAME, foreignKey = @javax.persistence.ForeignKey(name = "fk_areauserstats_areauser"))
    public AreaUser getAreaUser() {
        return areaUser;
    }

    public void setAreaUser(AreaUser areaUser) {
        this.areaUser = areaUser;
    }

    public CompositionConsumerCounts getCumulativeContentCounts() {
        return cumulativeContentCounts;
    }

    public void setCumulativeContentCounts(CompositionConsumerCounts cumulativeContentCounts) {
        this.cumulativeContentCounts = cumulativeContentCounts;
    }

    public CompositionConsumerCounts getCumulativeReplyCounts() {
        return cumulativeReplyCounts;
    }

    public void setCumulativeReplyCounts(CompositionConsumerCounts cumulativeReplyCounts) {
        this.cumulativeReplyCounts = cumulativeReplyCounts;
    }

    //@Temporal(TemporalType.TIMESTAMP)
    public Timestamp getLastLoginDatetime() {
        return lastLoginDatetime;
    }

    public void setLastLoginDatetime(Timestamp lastLoginDatetime) {
        this.lastLoginDatetime = lastLoginDatetime;
    }

    public Timestamp getPreviousLoginDatetime() {
        return previousLoginDatetime;
    }

    public void setPreviousLoginDatetime(Timestamp previousLoginDatetime) {
        this.previousLoginDatetime = previousLoginDatetime;
    }

    @Transient
    public void updateLoginTime(Timestamp newLogin) {
        if (getLastLoginDatetime() != null && newLogin.before(DateUtils.addHours(getLastLoginDatetime(), 1))) {
            return;
        }

        setPreviousLoginDatetime(getLastLoginDatetime());
        setLastLoginDatetime(newLogin);
    }

    @Transient
    public void addComment(CompositionConsumer compositionConsumer, Reply reply) {
        assert !NetworkRegistry.getInstance().isImporting() : "Should never attempt to add new reply during import!";
        assert dao().isLocked(this) : "Should only call addComment once you've already locked the AreaUserStats!";

        // only increment the cumulative user reply count when the reply
        // is going live for the first time.  this prevents us from incrementing
        // the cumulative user reply count when a reply is sent to the moderation
        // queue after having been live and then being re-approved.

        getCumulativeReplyCounts().addCompositionConsumerUsage(compositionConsumer);
    }

    @Transient
    public void addNewContent(CompositionConsumer compositionConsumer) {
        assert !NetworkRegistry.getInstance().isImporting() : "Should never attempt to add post count during import!";
        assert dao().isLocked(this) : "Should only call addNewContent once you've already locked the AreaUserStats!";
        CompositionConsumerType compositionConsumerType = compositionConsumer.getCompositionConsumerType();
        assert compositionConsumerType.getContentType() != null : "Only support clip and content for new content post counts! See AreaUserPointsEvent additions below.";
        assert compositionConsumerType.getContentType().isSupportsAuthor() : "All content for new content post count count/points should support author! See AreaUserPointsEvent additions below.";

        // only increment the cumulative user reply count when the reply
        // is going live for the first time.  this prevents us from incrementing
        // the cumulative user reply count when a reply is sent to the moderation
        // queue after having been live and then being re-approved.

        getCumulativeContentCounts().addCompositionConsumerUsage(compositionConsumer);
    }

    public static AreaUserStatsDAO dao() {
        return DAOImpl.getDAO(AreaUserStats.class);
    }
}
