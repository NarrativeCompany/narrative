package org.narrative.network.customizations.narrative.service.api.model;

import com.fasterxml.jackson.annotation.JsonTypeName;
import org.narrative.common.persistence.OID;
import org.narrative.network.customizations.narrative.serialization.jackson.annotation.JsonValueObject;
import lombok.Builder;
import lombok.Value;

/**
 * Date: 10/2/18
 * Time: 7:45 AM
 *
 * @author brian
 */
@JsonValueObject
@JsonTypeName("CurrentUserFollowedItem")
@Value
@Builder(toBuilder = true)
public class CurrentUserFollowedItemDTO {
    /**
     * since this object is only ever for the current user, the OID here is actually the Channel OID
     */
    private final OID oid;

    private final boolean followed;
}
