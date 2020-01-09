package org.narrative.network.core.content.base;

import org.narrative.common.persistence.DAOImpl;
import org.narrative.common.persistence.DAOObject;
import org.narrative.common.persistence.OID;
import org.narrative.common.persistence.hibernate.HibernateUtil;
import org.narrative.network.core.content.base.dao.FutureContentDAO;
import org.narrative.network.core.user.User;
import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import java.sql.Timestamp;

/**
 * Created by IntelliJ IDEA.
 * User: paul
 * Date: Nov 23, 2005
 * Time: 4:43:37 PM
 * To change this template use File | Settings | File Templates.
 */
@Entity
public class FutureContent implements DAOObject<FutureContentDAO> {
    private OID oid;
    private Content content;
    private OID userOid;
    private boolean isDraft;
    private Timestamp saveDatetime;

    public static final String FIELD__CONTENT__NAME = "content";
    public static final String FIELD__SAVE_DATETIME__NAME = "saveDatetime";

    /**
     * @deprecated for hibernate use only
     */
    public FutureContent() {}

    public FutureContent(Content content) {
        this.content = content;
        content.setFutureContent(this);
    }

    @Id
    @GeneratedValue(generator = HibernateUtil.FOREIGN_GENERIC_GENERATOR_NAME)
    @GenericGenerator(name = HibernateUtil.FOREIGN_GENERIC_GENERATOR_NAME, strategy = HibernateUtil.FOREIGN_STRATEGY, parameters = {@Parameter(name = HibernateUtil.FOREIGN_STRATEGY_PROPERTY_NAME, value = FIELD__CONTENT__NAME)})
    public OID getOid() {
        return oid;
    }

    public void setOid(OID oid) {
        this.oid = oid;
    }

    // lazy because this is never used
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @ForeignKey(name = HibernateUtil.NO_FOREIGN_KEY_NAME)
    @PrimaryKeyJoinColumn
    public Content getContent() {
        return content;
    }

    public void setContent(Content content) {
        this.content = content;
    }

    @NotNull
    public OID getUserOid() {
        return userOid;
    }

    public void setUserOid(OID userOid) {
        this.userOid = userOid;
    }

    @Transient
    public User getUser() {
        return User.dao().get(getUserOid());
    }

    @Transient
    public void setUser(User user) {
        setUserOid(user.getOid());
    }

    @NotNull
    public boolean isDraft() {
        return isDraft;
    }

    public void setDraft(boolean draft) {
        isDraft = draft;
    }

    @NotNull
    //@Temporal(TemporalType.TIMESTAMP)
    public Timestamp getSaveDatetime() {
        return saveDatetime;
    }

    public void setSaveDatetime(Timestamp saveDatetime) {
        this.saveDatetime = saveDatetime;
    }

    public static FutureContentDAO dao() {
        return DAOImpl.getDAO(FutureContent.class);
    }
}
