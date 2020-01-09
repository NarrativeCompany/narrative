package org.narrative.network.customizations.narrative.service.api.model;

import com.fasterxml.jackson.annotation.JsonTypeName;
import org.narrative.common.persistence.OID;
import org.narrative.network.customizations.narrative.channels.ChannelType;
import org.narrative.network.customizations.narrative.niches.niche.NicheStatus;
import org.narrative.network.customizations.narrative.serialization.jackson.annotation.JsonValueObject;
import lombok.Builder;
import lombok.Value;
import lombok.experimental.FieldNameConstants;

import java.time.Instant;

/**
 * Value object representing a Niche.
 */
@JsonValueObject
@JsonTypeName("Niche")
@Value
@FieldNameConstants
@Builder(toBuilder = true)
public class NicheDTO {
    private final OID oid;
    private final ChannelType type;
    private final String name;
    private final String description;
    private final NicheStatus status;
    private final String prettyUrlString;
    private final Instant renewalDatetime;
    private final CurrentUserFollowedItemDTO currentUserFollowedItem;
    private final UserDTO suggester;
    private final UserDTO owner;
}
