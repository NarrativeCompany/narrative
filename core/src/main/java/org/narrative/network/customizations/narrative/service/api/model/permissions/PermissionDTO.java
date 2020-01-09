package org.narrative.network.customizations.narrative.service.api.model.permissions;

import com.fasterxml.jackson.annotation.JsonTypeName;
import org.narrative.network.customizations.narrative.serialization.jackson.annotation.JsonValueObject;
import lombok.Builder;
import lombok.Data;

/**
 * Date: 10/17/18
 * Time: 5:25 PM
 *
 * @author brian
 */
@JsonValueObject
@JsonTypeName("Permission")
@Data
@Builder
public class PermissionDTO {
    private final boolean granted;
}
