package org.narrative.network.customizations.narrative.service.api.model;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import org.narrative.network.customizations.narrative.serialization.jackson.JacksonConst;
import org.narrative.network.customizations.narrative.serialization.jackson.annotation.JsonValueObject;
import lombok.Builder;
import lombok.Value;

/**
 * Date: 8/24/18
 * Time: 9:05 AM
 *
 * @author brian
 */
@JsonValueObject
@JsonTypeName("Error")
@Value
@Builder(toBuilder = true)
public class ErrorDTO {
    private ErrorType type;
    private String title;
    private String message;
    private String requiredPermission;
    private String referenceId;

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = JacksonConst.SER_TYPE_FIELD)
    private Object detail;
}
