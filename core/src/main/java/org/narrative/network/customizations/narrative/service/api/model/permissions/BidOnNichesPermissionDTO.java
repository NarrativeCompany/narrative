package org.narrative.network.customizations.narrative.service.api.model.permissions;

import com.fasterxml.jackson.annotation.JsonTypeName;
import org.narrative.network.customizations.narrative.serialization.jackson.annotation.JsonValueObject;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.Instant;

/**
 * Date: 10/17/18
 * Time: 5:29 PM
 *
 * @author brian
 */
@Data
@JsonValueObject
@JsonTypeName("BidOnNichesPermission")
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class BidOnNichesPermissionDTO extends RevokablePermissionDTO {
    @Builder(builderMethodName = "bidOnNichesBuilder")
    public BidOnNichesPermissionDTO(boolean granted, Instant restorationDatetime, RevokeReason revokeReason) {
        super(granted, restorationDatetime, revokeReason);
    }
}
