package org.narrative.network.customizations.narrative.service.api.model;

import com.fasterxml.jackson.annotation.JsonTypeName;
import org.narrative.network.customizations.narrative.serialization.jackson.annotation.JsonValueObject;
import lombok.Builder;
import lombok.Value;
import lombok.experimental.FieldNameConstants;

/**
 * Value object representing a user's niche slots.
 */
@JsonValueObject
@JsonTypeName("UserOwnedChannels")
@Value
@Builder(toBuilder = true)
@FieldNameConstants
public class UserOwnedChannelsDTO {
    /**
     * Number of niches owned by the user.
     */
    private final int ownedNiches;
    /**
     * Number of Publications owned by the user.
     */
    private final int ownedPublications;
}
