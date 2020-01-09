package org.narrative.network.customizations.narrative.service.api.model;

import com.fasterxml.jackson.annotation.JsonTypeName;
import org.narrative.common.persistence.OID;
import org.narrative.network.customizations.narrative.serialization.jackson.annotation.JsonValueObject;
import lombok.Builder;
import lombok.Value;
import lombok.experimental.FieldNameConstants;

/**
 * Date: 11/14/18
 * Time: 1:07 PM
 *
 * @author jonmark
 */
@JsonValueObject
@JsonTypeName("ElectionDetail")
@Value
@FieldNameConstants
@Builder(toBuilder = true)
public class ElectionDetailDTO {
    private final OID oid;

    private final ElectionDTO election;
    private final ElectionNomineeDTO currentUserNominee;
}
