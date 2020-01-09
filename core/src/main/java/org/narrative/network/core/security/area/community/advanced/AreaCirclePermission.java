package org.narrative.network.core.security.area.community.advanced;

import org.narrative.common.persistence.DAOObject;
import org.narrative.common.persistence.OID;
import org.narrative.common.persistence.OIDGenerator;
import org.narrative.network.core.security.area.community.advanced.dao.AreaCirclePermissionDAO;
import org.narrative.network.shared.daobase.NetworkDAOImpl;
import org.narrative.network.shared.security.Securable;
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
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Date: Mar 5, 2009
 * Time: 8:58:03 AM
 *
 * @author brian
 */
@Entity
@Proxy
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {AreaCirclePermission.FIELD__AREA_CIRCLE__COLUMN, AreaCirclePermission.FIELD__AREA_RESOURCE__COLUMN, AreaCirclePermission.FIELD__SECURABLE_TYPE__COLUMN})})
public class AreaCirclePermission implements DAOObject<AreaCirclePermissionDAO> {

    private OID oid;
    private AreaCircle areaCircle;
    private AreaResource areaResource;

    private int securableType;

    public static final String FIELD__AREA_CIRCLE__NAME = "areaCircle";
    public static final String FIELD__AREA_RESOURCE__NAME = "areaResource";
    public static final String FIELD__SECURABLE_TYPE__NAME = "securableType";

    public static final String FIELD__AREA_CIRCLE__COLUMN = FIELD__AREA_CIRCLE__NAME + "_" + FIELD__OID__NAME;
    public static final String FIELD__AREA_RESOURCE__COLUMN = FIELD__AREA_RESOURCE__NAME + "_" + AreaResource.FIELD__OID__NAME;
    public static final String FIELD__SECURABLE_TYPE__COLUMN = FIELD__SECURABLE_TYPE__NAME;

    /**
     * @deprecated for hibernate use only
     */
    public AreaCirclePermission() {}

    public AreaCirclePermission(AreaCircle areaCircle, AreaResource areaResource, Securable securable) {
        assert isEqual(areaCircle.getArea(), areaResource.getArea()) : "Areas must match for AreaCircle permissions!";
        assert isEqual(areaResource.getAreaResourceType(), securable.getAreaResourceType()) : "Should only ever construct AreaCirclePermissions with the same type of securable as the resource it is for. rt/" + areaResource.getAreaResourceType() + " st/" + securable.getAreaResourceType() + " s/" + securable;
        this.areaCircle = areaCircle;
        this.areaResource = areaResource;
        this.securable = securable;
        this.securableType = securable.getId();
    }

    @Id
    @GeneratedValue(generator = OIDGenerator.NAME)
    public OID getOid() {
        return oid;
    }

    public void setOid(OID oid) {
        this.oid = oid;
    }

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @ForeignKey(name = "fk_areaCirclePermission_group")
    public AreaCircle getAreaCircle() {
        return areaCircle;
    }

    public void setAreaCircle(AreaCircle areaCircle) {
        this.areaCircle = areaCircle;
    }

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @ForeignKey(name = "fk_areaCirclePermission_resource")
    public AreaResource getAreaResource() {
        return areaResource;
    }

    public void setAreaResource(AreaResource areaResource) {
        this.areaResource = areaResource;
    }

    public int getSecurableType() {
        return securableType;
    }

    /**
     *
     */
    public void setSecurableType(int securableType) {
        this.securableType = securableType;
    }

    private transient Securable securable;

    @Transient
    public Securable getSecurable() {
        if (securable == null) {
            securable = getAreaResource().getSecurable(getSecurableType());
        }
        return securable;
    }

    public static AreaCirclePermissionDAO dao() {
        return NetworkDAOImpl.getDAO(AreaCirclePermission.class);
    }
}
