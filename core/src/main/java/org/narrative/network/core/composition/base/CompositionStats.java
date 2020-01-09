package org.narrative.network.core.composition.base;

import org.narrative.common.persistence.DAOImpl;
import org.narrative.common.persistence.DAOObject;
import org.narrative.common.persistence.OID;
import org.narrative.common.persistence.hibernate.HibernateUtil;
import org.narrative.network.core.composition.base.dao.CompositionStatsDAO;
import org.narrative.network.shared.likes.LikeFields;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.ForeignKey;
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
import javax.validation.constraints.NotNull;

import java.sql.Timestamp;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: paul
 * Date: Oct 6, 2006
 * Time: 3:00:32 PM
 */
@Entity
@Proxy
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class CompositionStats implements DAOObject<CompositionStatsDAO> {
    private OID oid;
    private Composition composition;
    private int replyCount;
    private Timestamp lastUpdateDatetime;
    private Reply lastReply;
    private int pageViews;
    private int watchedContentCount;

    private LikeFields likeFields;

    public static final String FIELD__COMPOSITION__NAME = "composition";

    /**
     * @deprecated for hibernate use only
     */
    public CompositionStats() {}

    public CompositionStats(Composition composition) {
        this.composition = composition;
        likeFields = new LikeFields();
    }

    @Id
    @GeneratedValue(generator = HibernateUtil.FOREIGN_GENERIC_GENERATOR_NAME)
    @GenericGenerator(name = HibernateUtil.FOREIGN_GENERIC_GENERATOR_NAME, strategy = HibernateUtil.FOREIGN_STRATEGY, parameters = {@Parameter(name = HibernateUtil.FOREIGN_STRATEGY_PROPERTY_NAME, value = FIELD__COMPOSITION__NAME)})

    public OID getOid() {
        return oid;
    }

    public void setOid(OID oid) {
        this.oid = oid;
    }

    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = CompositionStats.FIELD__OID__NAME, foreignKey = @javax.persistence.ForeignKey(name = "fk_compositionstats_composition"))
    public Composition getComposition() {
        return composition;
    }

    public void setComposition(Composition composition) {
        this.composition = composition;
    }

    public int getReplyCount() {
        return replyCount;
    }

    public void setReplyCount(int replyCount) {
        this.replyCount = replyCount;
    }

    public Timestamp getLastUpdateDatetime() {
        return lastUpdateDatetime;
    }

    public void setLastUpdateDatetime(Timestamp lastUpdateDatetime) {
        this.lastUpdateDatetime = lastUpdateDatetime;
    }

    @OneToOne(optional = true)
    @ForeignKey(name = "fk_compositionStats_lastReply")
    public Reply getLastReply() {
        return lastReply;
    }

    public void setLastReply(Reply lastReply) {
        this.lastReply = lastReply;
    }

    public int getPageViews() {
        return pageViews;
    }

    public void setPageViews(int pageViews) {
        this.pageViews = pageViews;
    }

    public int getWatchedContentCount() {
        return watchedContentCount;
    }

    public void setWatchedContentCount(int watchedContentCount) {
        this.watchedContentCount = watchedContentCount;
    }

    @NotNull
    public LikeFields getLikeFields() {
        return likeFields;
    }

    public void setLikeFields(LikeFields likeFields) {
        this.likeFields = likeFields;
    }

    public static CompositionStatsDAO dao() {
        return DAOImpl.getDAO(CompositionStats.class);
    }

    @Transient
    public void addReply() {
        setReplyCount(Math.max(getReplyCount() + 1, 1));
    }

    private void removeReply() {
        setReplyCount(Math.max(getReplyCount() - 1, 0));
    }

    @Transient
    public void removeReply(Reply reply) {
        removeReply();
    }

    @Transient
    public void addToPageViewCount(int count) {
        pageViews += count;
    }

    @Transient
    public void incrementWatchedContent() {
        watchedContentCount++;
    }

    @Transient
    public void decrementWatchedContent() {
        watchedContentCount = Math.max(watchedContentCount - 1, 0);
    }
}
