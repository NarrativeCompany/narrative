package org.narrative.network.core.security.area.community.advanced.services;

import org.narrative.common.util.enums.EnumRegistry;
import org.narrative.network.core.area.base.Area;
import org.narrative.network.core.security.area.base.AreaRole;
import org.narrative.network.core.security.area.community.advanced.AreaCircle;
import org.narrative.network.core.security.area.community.advanced.AreaResource;
import org.narrative.network.core.security.area.community.advanced.AreaResourceType;
import org.narrative.network.core.user.AuthZone;
import org.narrative.network.customizations.narrative.service.api.model.permissions.PermissionDTO;
import org.narrative.network.shared.security.AccessViolation;
import org.narrative.network.shared.security.Securable;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import static org.narrative.common.util.CoreUtils.*;
import static org.narrative.network.shared.util.NetworkCoreUtils.*;

/**
 * Date: Mar 5, 2009
 * Time: 10:14:09 AM
 *
 * @author brian
 */
public enum GlobalSecurable implements Securable<Area> {
    PARTICIPATE_IN_TRIBUNAL_ACTIONS(73, true),
    REMOVE_AUP_VIOLATIONS(74, true);

    public static final Set<GlobalSecurable> NARRATIVE_PERMISSIONS = Collections.unmodifiableSet(EnumSet.of(PARTICIPATE_IN_TRIBUNAL_ACTIONS,REMOVE_AUP_VIOLATIONS));

    private final int id;
    private final boolean isAdminPermission;

    GlobalSecurable(int id, boolean isAdminPermission) {
        this.id = id;
        this.isAdminPermission = isAdminPermission;
    }

    static {
        // jw: lets pass this through EnumRegistry just to be sure that there are no duplicate ids
        assert EnumRegistry.getForId(GlobalSecurable.class, PARTICIPATE_IN_TRIBUNAL_ACTIONS.getId()) == PARTICIPATE_IN_TRIBUNAL_ACTIONS : "Expected to get PARTICIPATE_IN_TRIBUNAL_ACTIONS...  How did that not work?";
    }

    @Override
    public int getId() {
        return id;
    }

    public AreaResourceType getAreaResourceType() {
        return AreaResourceType.AREA;
    }

    @Override
    public int getOrdinal() {
        return ordinal();
    }

    public boolean isAdminSecurable() {
        return isAdminPermission;
    }

    @Override
    public boolean isValidForAreaResource(Area areaResourceImpl) {
        return true;
    }

    @Override
    public boolean isRequiredForAreaCircle(AreaCircle areaCircle) {
        return false;
    }

    @Override
    public boolean hasRight(AreaRole areaRole) {
        return hasRight(areaRole.getArea().getAreaResource(), areaRole);
    }

    @Override
    public void checkRight(AreaRole areaRole) {
        if (!hasRight(areaRole)) {
            throw new AccessViolation(this);
        }
    }

    @Override
    public boolean hasRight(Area areaResourceImpl, AreaRole areaRole) {
        return hasRight(areaResourceImpl.getAreaResource(), areaRole);
    }

    @Override
    public boolean hasRight(AreaResource areaResource, AreaRole areaRole) {
        assert areaResource.getAreaResourceType().isArea() : "Global security checks should only be performed with area AreaResources!";
        assert isEqual(areaResource.getArea(), areaRole.getArea()) : "Areas must match when doing security checks!";

        Area area = areaRole.getArea();

        if (exists(areaRole)) {
            return areaRole.hasRight(areaResource, this);
        }
        return false;
    }

    @Override
    public void checkRight(Area areaResourceImpl, AreaRole areaRole) {
        if (!hasRight(areaResourceImpl, areaRole)) {
            throw new AccessViolation(this);
        }
    }

    @Override
    public void checkRight(AreaResource areaResource, AreaRole areaRole) {
        assert areaResource.getAreaResourceType().isArea() : "Attempting to check a area right on a non area AreaResource";
        if (!hasRight(areaResource, areaRole)) {
            throw new AccessViolation(this);
        }
    }

    public boolean hasRight(AuthZone authZone, Set<Securable> grantedSecurables) {
        // permission is granted if you have the permission directly
        return grantedSecurables.contains(this);
    }

    @Override
    public String getNameForDisplay() {
        return wordlet("globalSecurable." + this);
    }

    @Override
    public String getDescription() {
        String descriptionKey = "globalSecurable." + this + ".description";
        // bl: only return a description if there is actually a description set for this permission.
        // not all permissions have a description, so this will allow us to optionally include a description
        // for each permission.
        if (networkContext().getResourceBundle().containsKey(descriptionKey)) {
            return wordlet(descriptionKey);
        }
        return null;
    }

    public PermissionDTO getPermissionDtoForAreaRole(AreaRole areaRole) {
        assert NARRATIVE_PERMISSIONS.contains(this) : "Should only get permission DTOs for Narrative permissions!";
        return PermissionDTO.builder()
                .granted(areaRole.hasGlobalRight(this))
                .build();
    }

    public boolean isParticipateInTribunalActions() {
        return this == PARTICIPATE_IN_TRIBUNAL_ACTIONS;
    }

}
