package org.narrative.network.customizations.narrative.service.api.model;

import com.fasterxml.jackson.annotation.JsonTypeName;
import org.narrative.common.persistence.OID;
import org.narrative.network.customizations.narrative.elections.NomineeStatus;
import org.narrative.network.customizations.narrative.serialization.jackson.annotation.JsonValueObject;
import lombok.Builder;
import lombok.Value;
import lombok.experimental.FieldNameConstants;

/**
 * Date: 11/14/18
 * Time: 12:52 PM
 *
 * @author jonmark
 */
@JsonValueObject
@JsonTypeName("ElectionNominee")
@Value
@FieldNameConstants
@Builder(toBuilder = true)
public class ElectionNomineeDTO {
    private final OID oid;

    private final UserDTO nominee;

    private final NomineeStatus status;
    private final String personalStatement;
}
