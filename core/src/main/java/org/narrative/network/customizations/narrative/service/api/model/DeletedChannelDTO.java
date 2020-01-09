package org.narrative.network.customizations.narrative.service.api.model;

import com.fasterxml.jackson.annotation.JsonTypeName;
import org.narrative.common.persistence.OID;
import org.narrative.network.customizations.narrative.channels.ChannelType;
import org.narrative.network.customizations.narrative.serialization.jackson.annotation.JsonValueObject;
import lombok.Builder;
import lombok.Value;
import lombok.experimental.FieldNameConstants;

/**
 * Date: 2019-10-03
 * Time: 15:00
 *
 * @author jonmark
 */
@JsonValueObject
@JsonTypeName("DeletedChannel")
@Value
@FieldNameConstants
@Builder
public class DeletedChannelDTO {
    private final OID oid;
    private final ChannelType type;
    private final String name;
    private final UserDTO owner;
}
