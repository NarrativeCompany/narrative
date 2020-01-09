package org.narrative.network.core.security.area.community.advanced;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.Proxy;
import org.hibernate.annotations.Type;
import org.narrative.common.persistence.DAOObject;
import org.narrative.common.persistence.OID;
import org.narrative.common.persistence.hibernate.IntegerEnumType;
import org.narrative.common.util.enums.EnumRegistry;
import org.narrative.network.core.area.base.Area;
import org.narrative.network.core.security.area.community.advanced.dao.AreaResourceDAO;
import org.narrative.network.shared.daobase.NetworkDAOImpl;
import org.narrative.network.shared.security.Securable;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.MapKey;
import javax.persistence.OneToMany;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import java.util.HashSet;
import java.util.Set;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Date: Mar 5, 2009
 * Time: 8:58:26 AM
 *
 * @author brian
 */
@Entity
@Proxy
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class AreaResource implements DAOObject<AreaResourceDAO> {

    private OID oid;
    private Area area;
    private AreaResourceType areaResourceType;

    private Set<AreaCirclePermission> permissions;

    private transient AreaResourceImpl areaResourceImpl;

    public static final String FIELD__AREA__NAME = "area";

    /**
     * @deprecated for hibernate use only
     */
    public AreaResource() {}

    public AreaResource(AreaResourceImpl areaResourceImpl) {
        this.oid = areaResourceImpl.getOid();
        this.area = areaResourceImpl.getArea();
        this.areaResourceType = areaResourceImpl.getAreaResourceType();
        this.permissions = new HashSet<>();
    }

    @Id
    public OID getOid() {
        return oid;
    }

    public void setOid(OID oid) {
        this.oid = oid;
    }

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @ForeignKey(name = "fk_areaResource_area")
    public Area getArea() {
        return area;
    }

    public void setArea(Area area) {
        this.area = area;
    }

    @NotNull
    @Type(type = IntegerEnumType.TYPE)
    public AreaResourceType getAreaResourceType() {
        return areaResourceType;
    }

    public void setAreaResourceType(AreaResourceType areaResourceType) {
        this.areaResourceType = areaResourceType;
    }

    @OneToMany(fetch = FetchType.LAZY, mappedBy = AreaCirclePermission.FIELD__AREA_RESOURCE__NAME, cascade = CascadeType.ALL)
    @MapKey(name = AreaCirclePermission.FIELD__AREA_CIRCLE__NAME)
    public Set<AreaCirclePermission> getPermissions() {
        return permissions;
    }

    public void setPermissions(Set<AreaCirclePermission> permissions) {
        this.permissions = permissions;
    }

    @Transient
    public Set<AreaCirclePermission> getPermissionsInited() {
        return initHibernateCollection(getPermissions());
    }

    @Transient
    public AreaResourceImpl getAreaResourceImpl() {
        if (areaResourceImpl == null) {
            areaResourceImpl = getAreaResourceType().getInstance(oid);
        }
        return areaResourceImpl;
    }

    @Transient
    public Securable getSecurable(int securableType) {
        return EnumRegistry.getForId(getAreaResourceType().getSecurableClass(), securableType);
    }

    public static boolean isSecurableValidForAreaResource(Securable securable, AreaResource areaResource) {
        return securable.isValidForAreaResource(areaResource.getAreaResourceImpl());
    }

    public static AreaResourceDAO dao() {
        return NetworkDAOImpl.getDAO(AreaResource.class);
    }
}
