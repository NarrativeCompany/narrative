package org.narrative.network.core.security.area.community.advanced;

import org.narrative.common.persistence.DAOObject;
import org.narrative.common.persistence.OID;
import org.narrative.common.persistence.OIDGenerator;
import org.narrative.network.core.area.user.AreaUser;
import org.narrative.network.core.security.area.community.advanced.dao.AreaCircleUserDAO;
import org.narrative.network.shared.daobase.NetworkDAOImpl;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.Proxy;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

/**
 * Created by IntelliJ IDEA.
 * User: jonmark
 * Date: 6/10/16
 * Time: 10:01 AM
 */
@Entity
@Proxy
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {AreaCircleUser.FIELD__AREA_CIRCLE__COLUMN, AreaCircleUser.FIELD__AREA_USER__COLUMN})})
public class AreaCircleUser implements DAOObject<AreaCircleUserDAO> {
    private OID oid;

    private AreaCircle areaCircle;
    private AreaUser areaUser;

    public static final String FIELD__AREA_CIRCLE__NAME = "areaCircle";
    public static final String FIELD__AREA_CIRCLE__COLUMN = FIELD__AREA_CIRCLE__NAME + "_" + AreaCircle.FIELD__OID__NAME;

    public static final String FIELD__AREA_USER__NAME = "areaUser";
    public static final String FIELD__AREA_USER__COLUMN = FIELD__AREA_USER__NAME + "_" + AreaUser.FIELD__OID__NAME;

    /**
     * @deprecated for hibernate use only
     */
    public AreaCircleUser() {}

    public AreaCircleUser(AreaCircle areaCircle, AreaUser areaUser) {
        this.areaCircle = areaCircle;
        this.areaUser = areaUser;
    }

    @Id
    @GeneratedValue(generator = OIDGenerator.NAME)
    public OID getOid() {
        return oid;
    }

    @Override
    public void setOid(OID oid) {
        this.oid = oid;
    }

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @ForeignKey(name = "fk_areaCircleUser_areaCircle")
    public AreaCircle getAreaCircle() {
        return areaCircle;
    }

    public void setAreaCircle(AreaCircle areaCircle) {
        this.areaCircle = areaCircle;
    }

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @ForeignKey(name = "fk_areaCircleUser_areaUser")
    public AreaUser getAreaUser() {
        return areaUser;
    }

    public void setAreaUser(AreaUser areaUser) {
        this.areaUser = areaUser;
    }

    public static AreaCircleUserDAO dao() {
        return NetworkDAOImpl.getDAO(AreaCircleUser.class);
    }
}
