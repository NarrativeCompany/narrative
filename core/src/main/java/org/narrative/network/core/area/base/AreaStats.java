package org.narrative.network.core.area.base;

import org.narrative.common.persistence.DAOImpl;
import org.narrative.common.persistence.DAOObject;
import org.narrative.common.persistence.OID;
import org.narrative.common.persistence.hibernate.HibernateUtil;
import org.narrative.network.core.area.base.dao.AreaStatsDAO;
import org.narrative.network.core.area.user.AreaUser;
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
import javax.validation.constraints.NotNull;

/**
 * Created by IntelliJ IDEA.
 * User: barry
 * Date: Jun 6, 2006
 * Time: 11:53:19 AM
 */
@Entity
@Proxy
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class AreaStats implements DAOObject<AreaStatsDAO> {
    private OID oid;
    private Area area;

    private int memberCount;

    public static final String FIELD__AREA__NAME = "area";

    @Deprecated
    public AreaStats() {}

    public AreaStats(Area area) {
        this.area = area;
    }

    @Id
    @GeneratedValue(generator = HibernateUtil.FOREIGN_GENERIC_GENERATOR_NAME)
    @GenericGenerator(name = HibernateUtil.FOREIGN_GENERIC_GENERATOR_NAME, strategy = HibernateUtil.FOREIGN_STRATEGY, parameters = {@Parameter(name = HibernateUtil.FOREIGN_STRATEGY_PROPERTY_NAME, value = FIELD__AREA__NAME)})
    public OID getOid() {
        return oid;
    }

    public void setOid(OID oid) {
        this.oid = oid;
    }

    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = AreaStats.FIELD__OID__NAME, foreignKey = @javax.persistence.ForeignKey(name = "fk_areastats_area"))
    public Area getArea() {
        return area;
    }

    public void setArea(Area area) {
        this.area = area;
    }

    @NotNull
    public int getMemberCount() {
        return memberCount;
    }

    /**
     * @param memberCount number of active members
     * @deprecated use updateMemberCount instead
     */
    public void setMemberCount(int memberCount) {
        this.memberCount = memberCount;
    }

    @Transient
    public void updateMemberCount() {
        setMemberCount(AreaUser.dao().getMemberCountForArea(getArea()));
    }

    public static AreaStatsDAO dao() {
        return DAOImpl.getDAO(AreaStats.class);
    }

}
