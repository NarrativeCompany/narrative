package org.narrative.network.customizations.narrative.service.api.model;

import com.fasterxml.jackson.annotation.JsonTypeName;
import org.narrative.network.customizations.narrative.serialization.jackson.annotation.JsonValueObject;
import lombok.Builder;
import lombok.Value;

import java.util.List;

/**
 * Date: 9/5/18
 * Time: 3:06 PM
 *
 * @author brian
 */
@JsonValueObject
@JsonTypeName("ValidationError")
@Value
@Builder(toBuilder = true)
public class ValidationErrorDTO {
    private final List<String> methodErrors;
    private final List<FieldErrorDTO> fieldErrors;
}
