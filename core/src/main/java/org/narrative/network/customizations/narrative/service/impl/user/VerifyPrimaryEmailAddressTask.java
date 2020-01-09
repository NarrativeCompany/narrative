package org.narrative.network.customizations.narrative.service.impl.user;

import org.narrative.common.util.ApplicationError;
import org.narrative.network.core.user.EmailAddress;
import org.narrative.network.core.user.EmailAddressVerificationStep;
import org.narrative.network.core.user.User;
import org.narrative.network.core.user.services.VerifyEmailAddressTask;
import org.narrative.network.customizations.narrative.service.api.ValidationContext;
import org.narrative.network.customizations.narrative.service.api.model.input.VerifyEmailAddressInput;
import org.narrative.network.shared.tasktypes.AreaTaskImpl;

import static org.narrative.network.shared.util.NetworkCoreUtils.*;

/**
 * Date: 10/1/18
 * Time: 8:41 AM
 *
 * @author brian
 */
public class VerifyPrimaryEmailAddressTask extends AreaTaskImpl<Object> {
    private final User user;
    private final String confirmationId;

    VerifyPrimaryEmailAddressTask(User user, VerifyEmailAddressInput input) {
        this.user = user;
        confirmationId = input.getConfirmationId();
    }

    @Override
    protected void validate(ValidationContext validationContext) {
        // jw: if we are not able to verify the token with the emailAddress being confirmed, then error out.
        if (!user.getUserFields().getEmailAddress().isConfirmationIdValid(EmailAddressVerificationStep.NEW_USER_STEP, confirmationId)) {
            throw new ApplicationError(wordlet("emailVerification.emailConfirmationIdInvalid"));
        }
    }

    @Override
    protected Object doMonitoredTask() {
        EmailAddress emailAddress = user.getUserFields().getEmailAddress();

        // jw: only do the verification if the user has not already verified. If they did, just let the request fall through
        if (!emailAddress.isVerified()) {
            getAreaContext().doAreaTask(new VerifyEmailAddressTask(user, emailAddress, EmailAddressVerificationStep.NEW_USER_STEP));
        }

        return null;
    }
}
