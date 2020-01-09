package org.narrative.network.customizations.narrative.controller.postbody.user;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.narrative.network.customizations.narrative.service.api.model.input.DisableTwoFactorAuthInput;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;

/**
 * Post body DTO for disabling two factor authentication.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Validated
public class DisableTwoFactorAuthInputDTO extends DisableTwoFactorAuthInput {
    @JsonCreator
    public DisableTwoFactorAuthInputDTO(@JsonProperty(Fields.currentPassword) @NotEmpty String currentPassword, @JsonProperty(Fields.twoFactorAuthCode) Integer twoFactorAuthCode) {
        super(currentPassword, twoFactorAuthCode);
    }
}
