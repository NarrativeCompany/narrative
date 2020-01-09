package org.narrative.network.customizations.narrative.permissions;

import org.narrative.network.core.security.area.base.AreaRole;
import org.narrative.network.customizations.narrative.service.api.model.permissions.RevokeReason;
import org.narrative.network.shared.security.AccessViolation;

import java.time.Instant;

/**
 * Date: 2019-02-10
 * Time: 16:08
 *
 * @author brian
 */
public interface NarrativePermissionCheck {
    void checkRight(AreaRole areaRole);

    default UserPermission getUserPermissionForAreaRole(AreaRole areaRole) {
        boolean granted = false;
        Instant restorationDatetime = null;
        RevokeReason revokeReason = null;
        try {
            checkRight(areaRole);
            // passed all checks, so the permission is granted!
            granted = true;
        } catch(NarrativePermissionRevokedError e) {
            // if the permission has been revoked, we'll use the revokeReason supplied in the exception
            revokeReason = e.getRevokeReason();
            restorationDatetime = e.getRestorationDatetime();
        } catch(AccessViolation av) {
            // no permission, revoke reason, or restoration datetime
        }

        return UserPermission.builder()
                .granted(granted)
                .restorationDatetime(restorationDatetime)
                .revokeReason(revokeReason)
                .build();
    }
}
