package org.narrative.network.customizations.narrative.controller.postbody.publication;

import org.narrative.common.persistence.OID;
import org.narrative.network.customizations.narrative.service.api.model.input.UpdateProfileAccountConfirmationInputBase;
import lombok.EqualsAndHashCode;
import lombok.Value;
import lombok.experimental.FieldNameConstants;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * Date: 9/12/19
 * Time: 4:11 PM
 *
 * @author brian
 */
@Value
@Validated
@FieldNameConstants
@EqualsAndHashCode(callSuper = true)
public class ChangePublicationOwnerInputDTO extends UpdateProfileAccountConfirmationInputBase {
    @NotNull
    private final OID userOid;

    public ChangePublicationOwnerInputDTO(@NotEmpty String currentPassword, Integer twoFactorAuthCode, @NotNull OID userOid) {
        super(currentPassword, twoFactorAuthCode);
        this.userOid = userOid;
    }
}
