package org.narrative.network.customizations.narrative.service.api.model;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Builder;
import lombok.Value;
import lombok.experimental.FieldNameConstants;
import org.narrative.network.customizations.narrative.serialization.jackson.annotation.JsonValueObject;

/**
 * Date: 2020-01-03
 * Time: 09:05
 *
 * @author jonmark
 */
@JsonValueObject
@JsonTypeName("SuspendEmailValidation")
@Value
@Builder
@FieldNameConstants
public class SuspendEmailValidationDTO {
    private String error;
    private UserDTO user;
}
