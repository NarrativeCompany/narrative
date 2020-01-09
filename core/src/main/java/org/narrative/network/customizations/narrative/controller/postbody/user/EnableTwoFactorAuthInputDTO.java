package org.narrative.network.customizations.narrative.controller.postbody.user;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.narrative.network.customizations.narrative.service.api.model.input.EnableTwoFactorAuthInput;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldNameConstants;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Validated
@FieldNameConstants
public class EnableTwoFactorAuthInputDTO extends EnableTwoFactorAuthInput {
    @JsonCreator
    public EnableTwoFactorAuthInputDTO(@JsonProperty(Fields.currentPassword) @NotEmpty String currentPassword,
                                       @JsonProperty(Fields.twoFactorAuthCode) @NotNull Integer twoFactorAuthCode,
                                       @JsonProperty(Fields.secret) @NotEmpty String secret,
                                       @JsonProperty(Fields.rememberMe) boolean rememberMe) {
        super(currentPassword, twoFactorAuthCode, secret, rememberMe);
    }
}
