package org.narrative.network.customizations.narrative.service.api.model.permissions;

import com.fasterxml.jackson.annotation.JsonTypeName;
import org.narrative.network.customizations.narrative.serialization.jackson.annotation.JsonValueObject;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.Instant;

/**
 * Date: 2019-02-13
 * Time: 15:59
 *
 * @author jonmark
 */
@Data
@JsonValueObject
@JsonTypeName("SubmitTribunalAppealsPermission")
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SubmitTribunalAppealsPermissionDTO extends RevokablePermissionDTO {
    @Builder(builderMethodName = "submitTribunalAppealsBuilder")
    public SubmitTribunalAppealsPermissionDTO(boolean granted, Instant restorationDatetime, RevokeReason revokeReason) {
        super(granted, restorationDatetime, revokeReason);
    }
}
