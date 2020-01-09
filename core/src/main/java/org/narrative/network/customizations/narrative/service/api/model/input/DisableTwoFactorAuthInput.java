package org.narrative.network.customizations.narrative.service.api.model.input;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Validated
public class DisableTwoFactorAuthInput extends UpdateProfileAccountConfirmationInputBase {
    @Builder
    public DisableTwoFactorAuthInput(@NotEmpty String currentPassword, Integer twoFactorAuthCode) {
        super(currentPassword, twoFactorAuthCode);
    }
}
