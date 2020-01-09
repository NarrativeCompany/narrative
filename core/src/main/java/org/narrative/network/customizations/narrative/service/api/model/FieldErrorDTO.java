package org.narrative.network.customizations.narrative.service.api.model;

import com.fasterxml.jackson.annotation.JsonTypeName;
import org.narrative.network.customizations.narrative.serialization.jackson.annotation.JsonValueObject;
import lombok.Builder;
import lombok.Value;

import java.util.List;

/**
 * Date: 9/11/18
 * Time: 3:16 PM
 *
 * @author brian
 */
@JsonValueObject
@JsonTypeName("FieldError")
@Value
@Builder(toBuilder = true)
public class FieldErrorDTO {
    private final String name;
    private final List<String> messages;
}
