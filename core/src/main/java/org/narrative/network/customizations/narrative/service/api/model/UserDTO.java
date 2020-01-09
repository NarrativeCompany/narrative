package org.narrative.network.customizations.narrative.service.api.model;

import com.fasterxml.jackson.annotation.JsonTypeName;
import org.narrative.common.persistence.OID;
import org.narrative.network.customizations.narrative.serialization.jackson.annotation.JsonValueObject;
import lombok.Builder;
import lombok.Value;

import java.util.List;

/**
 * Value object representing a User.
 */
@JsonValueObject
@JsonTypeName("User")
@Value
@Builder(toBuilder = true)
public class UserDTO {
    private final OID oid;
    private final String displayName;
    private final String username;
    private final String avatarSquareUrl;
    private final boolean deleted;
    private final List<String> labels;

    private final UserReputationDTO reputation;
}
