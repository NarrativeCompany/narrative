package org.narrative.network.customizations.narrative.service.api.model;

import com.fasterxml.jackson.annotation.JsonTypeName;
import org.narrative.common.persistence.OID;
import org.narrative.network.customizations.narrative.serialization.jackson.annotation.JsonValueObject;
import lombok.Builder;
import lombok.Value;

/**
 * Date: 2019-03-22
 * Time: 19:57
 *
 * @author jonmark
 */
@JsonValueObject
@JsonTypeName("FollowedUser")
@Value
@Builder(toBuilder = true)
public class FollowedUserDTO {
    private final OID oid;
    private final UserDTO user;
    private final CurrentUserFollowedItemDTO currentUserFollowedItem;
}
