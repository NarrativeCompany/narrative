package org.narrative.network.customizations.narrative.service.api.model;

import com.fasterxml.jackson.annotation.JsonTypeName;
import org.narrative.common.persistence.OID;
import org.narrative.network.customizations.narrative.serialization.jackson.annotation.JsonValueObject;
import lombok.Builder;
import lombok.Value;

import java.sql.Timestamp;

/**
 * Value object representing user stats.
 */
@JsonValueObject
@JsonTypeName("UserStats")
@Value
@Builder(toBuilder = true)
public class UserStatsDTO {
    private final OID oid;
    private final Timestamp lastLoginDatetime;
}
