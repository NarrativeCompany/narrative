package org.narrative.network.customizations.narrative.service.impl.user;

import org.narrative.common.util.ApplicationError;
import org.narrative.network.core.user.EmailAddress;
import org.narrative.network.core.user.EmailAddressVerificationStep;
import org.narrative.network.core.user.User;
import org.narrative.network.core.user.services.VerifyEmailAddressTask;
import org.narrative.network.customizations.narrative.service.api.ValidationContext;
import org.narrative.network.customizations.narrative.service.api.model.input.VerifyPendingEmailAddressInput;
import org.narrative.network.shared.tasktypes.AreaTaskImpl;

import static org.narrative.common.util.CoreUtils.*;
import static org.narrative.network.shared.util.NetworkCoreUtils.*;

/**
 * Date: 2019-07-15
 * Time: 07:48
 *
 * @author jonmark
 */
public class VerifyPendingEmailAddressTask extends AreaTaskImpl<EmailAddress> {
    private final User user;
    private final EmailAddressVerificationStep verificationStep;
    private final String confirmationId;
    private final EmailAddress emailAddress;

    VerifyPendingEmailAddressTask(User user, VerifyPendingEmailAddressInput input) {
        this.user = user;
        confirmationId = input.getConfirmationId();
        emailAddress = EmailAddress.dao().get(input.getEmailAddressOid());
        verificationStep = input.getVerificationStep();

        assert verificationStep != null : "We should always be given a verification step here!";
    }

    @Override
    protected void validate(ValidationContext validationContext) {
        // jw: the only way we should not get a emailAddress is if the link was for a pending email address that no longer
        //     exists because it was already verified and is now active, or it timed out and was removed.
        if (!exists(emailAddress)) {
            throw new ApplicationError(wordlet("emailVerification.emailConfirmationIdInvalid"));

        } else if (emailAddress.getVerifiedSteps().contains(verificationStep)) {
            throw new ApplicationError(wordlet("emailVerification.emailAddressedAlreadyConfirmed"));
        }

        // jw: if we are not able to verify the token with the emailAddress being confirmed, then error out.
        if (!emailAddress.isConfirmationIdValid(verificationStep, confirmationId)) {
            throw new ApplicationError(wordlet("emailVerification.emailConfirmationIdInvalid"));
        }
    }

    @Override
    protected EmailAddress doMonitoredTask() {
        // jw: let's use the utility task to verify the users email address
        getAreaContext().doAreaTask(new VerifyEmailAddressTask(user, emailAddress, verificationStep));

        return emailAddress;
    }
}
