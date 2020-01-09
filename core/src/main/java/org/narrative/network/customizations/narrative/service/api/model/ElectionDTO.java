package org.narrative.network.customizations.narrative.service.api.model;

import com.fasterxml.jackson.annotation.JsonTypeName;
import org.narrative.common.persistence.OID;
import org.narrative.network.customizations.narrative.elections.ElectionStatus;
import org.narrative.network.customizations.narrative.elections.ElectionType;
import org.narrative.network.customizations.narrative.serialization.jackson.annotation.JsonValueObject;
import lombok.Builder;
import lombok.Value;
import lombok.experimental.FieldNameConstants;

import java.time.Instant;

/**
 * Date: 11/13/18
 * Time: 3:26 PM
 *
 * @author jonmark
 */
@JsonValueObject
@JsonTypeName("Election")
@Value
@FieldNameConstants
@Builder(toBuilder = true)
public class ElectionDTO {
    private final OID oid;
    private final ElectionType type;
    private final ElectionStatus status;
    private final Instant nominationStartDatetime;
    private final int availableSlots;
    private final int nomineeCount;
}
