package org.narrative.network.customizations.narrative.permissions;

import org.narrative.network.customizations.narrative.service.api.model.permissions.RevokeReason;
import org.narrative.network.shared.security.AccessViolation;

import java.time.Instant;

/**
 * Date: 10/17/18
 * Time: 6:13 PM
 *
 * @author brian
 */
public class NarrativePermissionRevokedError extends AccessViolation {
    private final RevokeReason revokeReason;
    private final Instant restorationDatetime;

    public NarrativePermissionRevokedError(String title, String message, RevokeReason revokeReason, Instant restorationDatetime) {
        super(title, message);
        this.revokeReason = revokeReason;
        this.restorationDatetime = restorationDatetime;
    }

    public RevokeReason getRevokeReason() {
        return revokeReason;
    }

    public Instant getRestorationDatetime() {
        return restorationDatetime;
    }
}
