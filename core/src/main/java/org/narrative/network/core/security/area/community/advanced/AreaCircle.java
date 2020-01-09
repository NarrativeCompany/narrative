package org.narrative.network.core.security.area.community.advanced;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.Proxy;
import org.hibernate.validator.constraints.Length;
import org.narrative.common.persistence.DAOObject;
import org.narrative.common.persistence.OID;
import org.narrative.common.persistence.OIDGenerator;
import org.narrative.common.util.NarrativeConstants;
import org.narrative.network.core.area.base.Area;
import org.narrative.network.core.security.area.community.advanced.dao.AreaCircleDAO;
import org.narrative.network.core.user.AuthZone;
import org.narrative.network.shared.daobase.NetworkDAOImpl;
import org.narrative.network.shared.security.Securable;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Date: Mar 5, 2009
 * Time: 8:54:01 AM
 *
 * @author brian
 */
@Entity
@Proxy
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class AreaCircle implements DAOObject<AreaCircleDAO> {

    private OID oid;
    private Area area;
    private String name;
    private String label;
    private boolean isViewableByAdminsOnly;

    private Set<AreaCirclePermission> permissions;

    private Set<AreaCircleUser> areaCircleUsers;

    public static final String FIELD__AREA__NAME = "area";

    /**
     * @deprecated for hibernate use only
     */
    public AreaCircle() {}

    public AreaCircle(Area area) {
        this.area = area;
        permissions = new HashSet<>();
        // jw: lets default the viewable by admins only to true for everything
        isViewableByAdminsOnly = true;
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
    @ForeignKey(name = "fk_areaCircle_area")
    public Area getArea() {
        return area;
    }

    public void setArea(Area area) {
        this.area = area;
    }

    @Transient
    public AuthZone getAuthZone() {
        return getArea().getAuthZone();
    }

    public static final int MIN_NAME_LENGTH = 1;
    public static final int MAX_NAME_LENGTH = NarrativeConstants.MAX_VARCHAR_MYSQL_FIELD_LENGTH;

    @NotNull
    @Length(min = MIN_NAME_LENGTH, max = MAX_NAME_LENGTH)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public static final int MIN_LABEL_LENGTH = 0;
    public static final int MAX_LABEL_LENGTH = 20;

    @Length(min = MIN_LABEL_LENGTH, max = MAX_LABEL_LENGTH)
    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public boolean isViewableByAdminsOnly() {
        return isViewableByAdminsOnly;
    }

    public void setViewableByAdminsOnly(boolean viewableByAdminsOnly) {
        isViewableByAdminsOnly = viewableByAdminsOnly;
    }

    @OneToMany(fetch = FetchType.LAZY, mappedBy = AreaCirclePermission.FIELD__AREA_CIRCLE__NAME, cascade = javax.persistence.CascadeType.ALL)
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    public Set<AreaCirclePermission> getPermissions() {
        return permissions;
    }

    public void setPermissions(Set<AreaCirclePermission> areaCirclePermissions) {
        this.permissions = areaCirclePermissions;
    }

    @Transient
    public Set<AreaCirclePermission> getPermissionsInited() {
        return initHibernateCollection(getPermissions());
    }

    private transient Map<AreaResource, Set<AreaCirclePermission>> permissionsByResource;

    @Transient
    public Map<AreaResource, Set<AreaCirclePermission>> getPermissionByResource() {
        if (permissionsByResource == null) {
            Map<AreaResource, Set<AreaCirclePermission>> lookup = new HashMap<>();
            for (AreaCirclePermission permission : getPermissionsInited()) {
                addMapSetLookupValue(lookup, permission.getAreaResource(), permission);
            }

            permissionsByResource = lookup;
        }

        return permissionsByResource;
    }

    public Set<AreaCirclePermission> getPermissionsForResource(AreaResource resource) {
        Set<AreaCirclePermission> permissions = getPermissionByResource().get(resource);
        if (permissions == null) {
            return Collections.emptySet();
        }

        return permissions;
    }

    private transient Map<AreaResource, Set<Securable>> securablesByResource;

    @Transient
    public Map<AreaResource, Set<Securable>> getSecurablesByResource() {
        if (securablesByResource == null) {
            Map<AreaResource, Set<Securable>> lookup = new HashMap<>();
            for (AreaCirclePermission permission : getPermissionsInited()) {
                addMapSetLookupValue(lookup, permission.getAreaResource(), permission.getSecurable());
            }
            securablesByResource = lookup;
        }

        return securablesByResource;
    }

    private Set<Securable> getSecurablesForResource(AreaResource areaResource) {
        Set<Securable> securables = getSecurablesByResource().get(areaResource);
        if (securables == null) {
            return Collections.emptySet();
        }

        return securables;
    }

    // jw: this method will force the transient caches to be re-generated.
    public void invalidateSecurableCaches() {
        permissionsByResource = null;
        securablesByResource = null;
    }

    @Transient
    public boolean isPermissionGranted(AreaResource resource, Securable securable) {
        return securable.hasRight(resource.getArea().getAuthZone(), getSecurablesForResource(resource));
    }

    @OneToMany(fetch = FetchType.LAZY, mappedBy = AreaCircleUser.FIELD__AREA_CIRCLE__NAME, cascade = javax.persistence.CascadeType.ALL)
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    public Set<AreaCircleUser> getAreaCircleUsers() {
        return areaCircleUsers;
    }

    public void setAreaCircleUsers(Set<AreaCircleUser> areaCircleUsers) {
        this.areaCircleUsers = areaCircleUsers;
    }

    @Transient
    public void addSecurables(AreaResource areaResource, Set<Securable> securables) {
        Set<AreaCirclePermission> permissions = getPermissionsForResource(areaResource);

        // jw: let's iterate over all of the securables that are being added
        for (Securable securable : securables) {
            // jw: if the securable is already set on the areaCircle then lets go ahead and short out, nothing to do.
            if (isHasSecurable(permissions, securable)) {
                continue;
            }

            AreaCirclePermission permission = new AreaCirclePermission(this, areaResource, securable);

            // jw: we need to add this to both the AreaCircle and the AreaResource permissions collections!
            areaResource.getPermissionsInited().add(permission);
            getPermissionsInited().add(permission);
        }

        // jw: be sure to invalidate the lookup caches since those could be used for permission checks later.
        invalidateSecurableCaches();
    }

    private static boolean isHasSecurable(Set<AreaCirclePermission> permissions, Securable securable) {
        for (AreaCirclePermission permission : permissions) {
            if (isEqual(permission.getSecurable(), securable)) {
                return true;
            }
        }
        return false;
    }

    @Transient
    public void addSecurable(AreaResource areaResource, Securable securable) {
        addSecurables(areaResource, Collections.singleton(securable));
    }

    @Transient
    public String getCssClassSuffix() {
        return getOid().toString();
    }

    public static AreaCircleDAO dao() {
        return NetworkDAOImpl.getDAO(AreaCircle.class);
    }
}
