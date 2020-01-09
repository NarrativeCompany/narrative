package org.narrative.network.customizations.narrative.service.api.model;

import com.fasterxml.jackson.annotation.JsonTypeName;
import org.narrative.common.persistence.OID;
import org.narrative.network.customizations.narrative.serialization.jackson.annotation.JsonValueObject;
import lombok.Builder;
import lombok.Value;
import lombok.experimental.FieldNameConstants;

/**
 * Date: 11/13/18
 * Time: 3:24 PM
 *
 * @author jonmark
 */
@JsonValueObject
@JsonTypeName("NicheModeratorElection")
@Value
@FieldNameConstants
@Builder(toBuilder = true)
public class NicheModeratorElectionDTO {
    private final OID oid;
    private final ElectionDTO election;
    private final NicheDTO niche;
}
