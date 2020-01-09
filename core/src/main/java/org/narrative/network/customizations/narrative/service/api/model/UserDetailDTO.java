package org.narrative.network.customizations.narrative.service.api.model;

import com.fasterxml.jackson.annotation.JsonTypeName;
import org.narrative.network.customizations.narrative.serialization.jackson.annotation.JsonValueObject;
import lombok.Builder;
import lombok.Value;

import java.sql.Timestamp;

@JsonValueObject
@JsonTypeName("UserDetail")
@Value
@Builder(toBuilder = true)
public class UserDetailDTO {
    private final UserDTO user;
    private final Timestamp joined;
    private final Timestamp lastVisit;
    private final boolean hideMyFollowers;
    private final boolean hideMyFollows;
}
