package org.narrative.network.customizations.narrative.service.api.model;

import com.fasterxml.jackson.annotation.JsonTypeName;
import org.narrative.network.customizations.narrative.serialization.jackson.annotation.JsonValueObject;
import lombok.Builder;
import lombok.Value;

/**
 * Date: 9/11/18
 * Time: 10:28 AM
 *
 * @author brian
 */
@JsonValueObject
@JsonTypeName("Token")
@Value
@Builder(toBuilder = true)
public class TokenDTO {
    private final String token;
    private final Boolean twoFactorAuthExpired;
}
