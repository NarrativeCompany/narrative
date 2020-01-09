package org.narrative.network.customizations.narrative.permissions;

import org.narrative.network.customizations.narrative.service.api.model.permissions.RevokeReason;
import lombok.Builder;
import lombok.Value;

import java.time.Instant;

/**
 * Date: 2019-02-10
 * Time: 16:15
 *
 * @author brian
 */
@Value
@Builder
public class UserPermission {
    private final boolean granted;
    private final Instant restorationDatetime;
    private final RevokeReason revokeReason;
}
