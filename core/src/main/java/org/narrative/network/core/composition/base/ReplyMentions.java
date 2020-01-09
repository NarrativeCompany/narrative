package org.narrative.network.core.composition.base;

import org.narrative.common.persistence.OID;
import org.narrative.common.persistence.hibernate.HibernateUtil;
import org.narrative.network.core.composition.base.dao.ReplyMentionsDAO;
import org.narrative.network.shared.daobase.NetworkDAOImpl;
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
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Transient;

/**
 * Created by IntelliJ IDEA.
 * User: jonmark
 * Date: 7/26/16
 * Time: 1:34 PM
 */
@Entity
@Proxy
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class ReplyMentions extends PostMentionsBase<ReplyMentionsDAO> {
    private OID oid;
    private Reply reply;

    public static final String FIELD__REPLY__NAME = "reply";

    /**
     * @deprecated For Hibernate use only!
     */
    public ReplyMentions() {}

    public ReplyMentions(Reply reply) {
        this.reply = reply;
    }

    @Id
    @GeneratedValue(generator = HibernateUtil.FOREIGN_GENERIC_GENERATOR_NAME)
    @GenericGenerator(name = HibernateUtil.FOREIGN_GENERIC_GENERATOR_NAME, strategy = HibernateUtil.FOREIGN_STRATEGY, parameters = {@Parameter(name = HibernateUtil.FOREIGN_STRATEGY_PROPERTY_NAME, value = FIELD__REPLY__NAME)})
    public OID getOid() {
        return oid;
    }

    @Override
    public void setOid(OID oid) {
        this.oid = oid;
    }

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @ForeignKey(name = HibernateUtil.NO_FOREIGN_KEY_NAME)
    @PrimaryKeyJoinColumn
    public Reply getReply() {
        return reply;
    }

    public void setReply(Reply reply) {
        this.reply = reply;
    }

    @Override
    @Transient
    protected PostBase getPostBase() {
        return getReply();
    }

    public static ReplyMentionsDAO dao() {
        return NetworkDAOImpl.getDAO(ReplyMentions.class);
    }
}
