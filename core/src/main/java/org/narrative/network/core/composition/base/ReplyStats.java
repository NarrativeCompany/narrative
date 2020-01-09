package org.narrative.network.core.composition.base;

import org.narrative.common.persistence.DAOObject;
import org.narrative.common.persistence.OID;
import org.narrative.common.persistence.hibernate.HibernateUtil;
import org.narrative.network.core.composition.base.dao.ReplyStatsDAO;
import org.narrative.network.shared.daobase.NetworkDAOImpl;
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
import javax.validation.constraints.NotNull;

/**
 * Date: May 4, 2010
 * Time: 6:05:07 PM
 *
 * @author brian
 */
@Entity
@Proxy
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class ReplyStats implements DAOObject<ReplyStatsDAO> {

    private OID oid;
    private Reply reply;
    private int reportCount;
    private int likeCount;

    public static final String FIELD__REPLY__NAME = "reply";

    /**
     * @deprecated for hibernate use only
     */
    public ReplyStats() {}

    public ReplyStats(Reply reply) {
        this.reply = reply;
    }

    public ReplyStats(Reply reply, ReplyStats replyStats) {
        this.reply = reply;
        this.reportCount = replyStats.reportCount;
        this.likeCount = replyStats.likeCount;
    }

    @Id
    @GeneratedValue(generator = HibernateUtil.FOREIGN_GENERIC_GENERATOR_NAME)
    @GenericGenerator(name = HibernateUtil.FOREIGN_GENERIC_GENERATOR_NAME, strategy = HibernateUtil.FOREIGN_STRATEGY, parameters = {@Parameter(name = HibernateUtil.FOREIGN_STRATEGY_PROPERTY_NAME, value = FIELD__REPLY__NAME)})
    public OID getOid() {
        return oid;
    }

    public void setOid(OID oid) {
        this.oid = oid;
    }

    @MapsId
    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = ReplyStats.FIELD__OID__NAME, foreignKey = @javax.persistence.ForeignKey(name = "fk_replystats_reply"))
    public Reply getReply() {
        return reply;
    }

    public void setReply(Reply reply) {
        this.reply = reply;
    }

    @NotNull
    public int getReportCount() {
        return reportCount;
    }

    public void setReportCount(int reportCount) {
        this.reportCount = reportCount;
    }

    public int getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(int likeCount) {
        this.likeCount = likeCount;
    }

    public static ReplyStatsDAO dao() {
        return NetworkDAOImpl.getDAO(ReplyStats.class);
    }
}
