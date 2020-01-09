package org.narrative.network.customizations.narrative.service.api.model.permissions;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldNameConstants;

import java.time.Instant;

/**
 * Date: 10/17/18
 * Time: 5:26 PM
 *
 * @author brian
 */
@Data
@FieldNameConstants
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class RevokablePermissionDTO extends PermissionDTO {
    private final Instant restorationDatetime;
    private final RevokeReason revokeReason;

    RevokablePermissionDTO(boolean granted, Instant restorationDatetime, RevokeReason revokeReason) {
        super(granted);
        this.restorationDatetime = restorationDatetime;
        this.revokeReason = revokeReason;
    }
}
