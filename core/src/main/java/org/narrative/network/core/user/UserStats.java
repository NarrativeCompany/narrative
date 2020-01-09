package org.narrative.network.core.user;

import org.narrative.common.persistence.DAOImpl;
import org.narrative.common.persistence.DAOObject;
import org.narrative.common.persistence.OID;
import org.narrative.common.persistence.hibernate.HibernateUtil;
import org.narrative.network.core.user.dao.UserStatsDAO;
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

import java.sql.Timestamp;

/**
 * Created by IntelliJ IDEA.
 * User: paul
 * Date: Nov 23, 2005
 * Time: 3:22:51 PM
 */
@Entity
@Proxy
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class UserStats implements DAOObject<UserStatsDAO> {

    private OID oid;
    private User user;
    private Timestamp lastLoginDatetime;

    public static final String FIELD__USER__NAME = "user";

    /**
     * @deprecated for hibernate use only
     */
    public UserStats() {}

    public UserStats(User user) {
        this.user = user;
    }

    @Id
    @GeneratedValue(generator = HibernateUtil.FOREIGN_GENERIC_GENERATOR_NAME)
    @GenericGenerator(name = HibernateUtil.FOREIGN_GENERIC_GENERATOR_NAME, strategy = HibernateUtil.FOREIGN_STRATEGY, parameters = {@Parameter(name = HibernateUtil.FOREIGN_STRATEGY_PROPERTY_NAME, value = FIELD__USER__NAME)})
    public OID getOid() {
        return oid;
    }

    public void setOid(OID oid) {
        this.oid = oid;
    }

    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = UserStats.FIELD__OID__NAME, foreignKey = @javax.persistence.ForeignKey(name = "fk_userstats_user"))
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Timestamp getLastLoginDatetime() {
        return lastLoginDatetime;
    }

    public void setLastLoginDatetime(Timestamp lastLoginDatetime) {
        this.lastLoginDatetime = lastLoginDatetime;
    }

    public static UserStatsDAO dao() {
        return DAOImpl.getDAO(UserStats.class);
    }

}
