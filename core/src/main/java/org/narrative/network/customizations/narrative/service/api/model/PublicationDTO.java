package org.narrative.network.customizations.narrative.service.api.model;

import com.fasterxml.jackson.annotation.JsonTypeName;
import org.narrative.common.persistence.OID;
import org.narrative.network.customizations.narrative.channels.ChannelType;
import org.narrative.network.customizations.narrative.publications.PublicationStatus;
import org.narrative.network.customizations.narrative.serialization.jackson.annotation.JsonValueObject;
import lombok.Builder;
import lombok.Value;
import lombok.experimental.FieldNameConstants;

/**
 * Date: 2019-07-31
 * Time: 12:53
 *
 * @author jonmark
 */
@JsonValueObject
@JsonTypeName("Publication")
@Value
@Builder(toBuilder = true)
@FieldNameConstants
public class PublicationDTO {
    private final OID oid;
    private final ChannelType type;
    private final String name;
    private final String description;
    private final String prettyUrlString;
    private final String logoUrl;
    private final PublicationStatus status;

    private final CurrentUserFollowedItemDTO currentUserFollowedItem;
}
