package org.narrative.network.customizations.narrative.service.api.model;

import com.fasterxml.jackson.annotation.JsonTypeName;
import org.narrative.common.persistence.OID;
import org.narrative.network.customizations.narrative.serialization.jackson.annotation.JsonValueObject;
import lombok.Builder;
import lombok.Value;
import lombok.experimental.FieldNameConstants;

/**
 * Date: 11/14/18
 * Time: 1:06 PM
 *
 * @author jonmark
 */
@JsonValueObject
@JsonTypeName("NicheModeratorElectionDetail")
@Value
@FieldNameConstants
@Builder(toBuilder = true)
public class NicheModeratorElectionDetailDTO {
    private final OID oid;
    private final ElectionDetailDTO election;
    private final NicheDTO niche;
}
