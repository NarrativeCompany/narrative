package org.narrative.network.core.security.area.base;

import org.narrative.network.core.area.base.Area;
import org.narrative.network.core.area.user.AreaUser;
import org.narrative.network.core.area.user.AreaUserRlm;
import org.narrative.network.core.security.area.community.advanced.AreaCircle;
import org.narrative.network.core.security.area.community.advanced.AreaResource;
import org.narrative.network.core.security.area.community.advanced.services.GlobalSecurable;
import org.narrative.network.core.user.services.preferences.FormatPreferences;
import org.narrative.network.customizations.narrative.permissions.NarrativePermissionType;
import org.narrative.network.shared.security.AccessViolation;
import org.narrative.network.shared.security.AuthZoneLoginRequired;
import org.narrative.network.shared.security.Role;
import org.narrative.network.shared.security.Securable;
import org.jetbrains.annotations.Nullable;

import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

import java.util.Set;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Created by IntelliJ IDEA.
 * User: paul
 * Date: Dec 16, 2005
 * Time: 12:10:38 PM
 */
@MappedSuperclass
public abstract class AreaRole implements Role {

    /**
     * @deprecated for hibernate use only
     */
    protected AreaRole() {
    }

    protected AreaRole(boolean init) {

    }

    @Transient
    public boolean hasRight(AreaResource areaResource, Securable securable) {
        assert isEqual(getArea(), areaResource.getArea()) : "Area mismatch when getting AreaRole permission bitmask!";
        Set<AreaCircle> areaCircles = getEffectiveAreaCircles();
        // bl: check to see if the permission is granted by any of the area groups.
        for (AreaCircle areaCircle : areaCircles) {
            if (areaCircle.isPermissionGranted(areaResource, securable)) {
                return true;
            }
        }
        return false;
    }

    public final boolean hasGlobalRight(GlobalSecurable securable) {
        return securable.hasRight(this);
    }

    public final void checkGlobalRight(GlobalSecurable securable) {
        securable.checkRight(this);
    }

    public final boolean hasNarrativeRight(NarrativePermissionType permission) {
        try {
            checkNarrativeRight(permission);
            return true;
        } catch(AccessViolation av) {
            return false;
        }
    }

    public final void checkNarrativeRight(NarrativePermissionType permissionType) {
        permissionType.checkRight(this);
    }

    public void checkRegisteredCommunityUser() {
        if (!isAnAreaUser()) {
            throw new AuthZoneLoginRequired();
        } else if (isActiveRegisteredAreaUser() && getUser().isPendingEmailVerification()) {
            throw new AccessViolation(getUser().getPendingRegistrationMessage(false));
        }
    }

    @Transient
    public FormatPreferences getFormatPreferences() {
        return getPrimaryRole().getFormatPreferences();
    }

    @Transient
    public abstract Area getArea();

    @Transient
    @Nullable
    public abstract AreaUserRlm getAreaUserRlm();

    @Transient
    public abstract AreaUser getAreaUser();

    @Transient
    public abstract boolean isAnAreaUser();

    @Transient
    public abstract boolean isActiveRegisteredAreaUser();

    @Transient
    public abstract Set<AreaCircle> getEffectiveAreaCircles();

}
