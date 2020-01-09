package org.narrative.network.customizations.narrative.controller.postbody.user;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.narrative.common.persistence.OID;
import org.narrative.network.core.user.EmailAddressVerificationStep;
import org.narrative.network.customizations.narrative.service.api.model.input.VerifyPendingEmailAddressInput;

/**
 * Date: 2019-07-12
 * Time: 14:34
 *
 * @author jonmark
 */
public class VerifyPendingEmailAddressInputDTO extends VerifyPendingEmailAddressInput {
    @JsonCreator
    public VerifyPendingEmailAddressInputDTO(
            @JsonProperty(Fields.confirmationId) String confirmationId,
            @JsonProperty(Fields.emailAddressOid) OID emailAddressOid,
            @JsonProperty(Fields.verificationStep) EmailAddressVerificationStep verificationStep
    ) {
        super(confirmationId, emailAddressOid, verificationStep);
    }
}
