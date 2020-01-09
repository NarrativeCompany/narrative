package org.narrative.network.customizations.narrative.service.api.model.input;

import org.narrative.common.persistence.OID;
import org.narrative.network.core.user.EmailAddressVerificationStep;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldNameConstants;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * Date: 2019-07-12
 * Time: 14:27
 *
 * @author jonmark
 */
@Data
@Validated
@FieldNameConstants
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class VerifyPendingEmailAddressInput extends VerifyEmailAddressInput {
    @NotNull
    private final OID emailAddressOid;
    @NotNull
    private final EmailAddressVerificationStep verificationStep;

    public VerifyPendingEmailAddressInput(
            @NotEmpty String confirmationId,
            @NotNull OID emailAddressOid,
            @NotNull EmailAddressVerificationStep verificationStep) {
        super(confirmationId);
        this.emailAddressOid = emailAddressOid;
        this.verificationStep = verificationStep;
    }

    /**
     * bl: Lombok will add the field name constants to this class, but i'm defining them explicitly
     * so that we have inheritance of Fields from superclasses (and subclasses can use it accordingly)
     * lombok feature request: https://github.com/rzwitserloot/lombok/issues/2090
     */
    public static class Fields extends VerifyEmailAddressInput.Fields {}
}

