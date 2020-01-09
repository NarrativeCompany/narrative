package org.narrative.network.customizations.narrative.service.impl.user;

import org.narrative.common.util.ApplicationError;
import org.narrative.network.core.user.EmailAddress;
import org.narrative.network.core.user.EmailAddressVerificationStep;
import org.narrative.network.core.user.User;
import org.narrative.network.customizations.narrative.controller.result.ScalarResultDTO;
import org.narrative.network.customizations.narrative.service.api.ValidationContext;
import org.narrative.network.customizations.narrative.service.api.model.input.VerifyPendingEmailAddressInput;
import org.narrative.network.shared.tasktypes.AreaTaskImpl;

import static org.narrative.common.util.CoreUtils.*;
import static org.narrative.network.shared.util.NetworkCoreUtils.*;

/**
 * Date: 2019-07-11
 * Time: 15:27
 *
 * @author jonmark
 */
public class CancelUserEmailAddressChangeTask extends AreaTaskImpl<ScalarResultDTO<String>> {
    private final EmailAddressVerificationStep verificationStep;
    private final String confirmationId;
    private final EmailAddress emailAddress;

    CancelUserEmailAddressChangeTask(User user, VerifyPendingEmailAddressInput input) {
        confirmationId = input.getConfirmationId();
        verificationStep = input.getVerificationStep();
        emailAddress = EmailAddress.dao().get(input.getEmailAddressOid());

        assert verificationStep != null : "We should always have a verifiedEmailAddressType when cancelling a email address change.";
    }

    @Override
    protected void validate(ValidationContext validationContext) {
        // jw: If there is no pending email address then there is nothing for the user to do, and this code, no matter
        //     what it is, is invalid.
        if (!exists(emailAddress)) {
            throw new ApplicationError(wordlet("cancelUserEmailAddressChangeTask.emailConfirmationIdInvalid"));

        // jw: if the email has been verified and is not their primary email address we need to let them know that.
        } else if (emailAddress.getType().isPrimary()) {
            throw new ApplicationError(wordlet("cancelUserEmailAddressChangeTask.emailIsAlreadyVerified"));
        }

        // jw: if we are not able to verify the token with the pending emailAddress, then error out.
        if (!emailAddress.isConfirmationIdValid(verificationStep, confirmationId)) {
            throw new ApplicationError(wordlet("emailVerification.emailConfirmationIdInvalid"));
        }
    }

    @Override
    protected ScalarResultDTO<String> doMonitoredTask() {
        // jw: this is shockingly simple, just delete the pending email address and we are done!
        EmailAddress.dao().delete(emailAddress);

        return ScalarResultDTO.<String>builder().value(emailAddress.getEmailAddress()).build();
    }
}
