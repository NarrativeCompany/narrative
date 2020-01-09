package org.narrative.network.customizations.narrative.service.impl.user;

import org.narrative.common.util.MailUtil;
import org.narrative.network.core.master.manage.profile.services.StartEmailChangeProcessTask;
import org.narrative.network.core.user.EmailAddress;
import org.narrative.network.core.user.User;
import org.narrative.network.customizations.narrative.service.api.ValidationContext;
import org.narrative.network.customizations.narrative.service.api.model.input.UpdateEmailAddressInput;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Date: 9/28/18
 * Time: 7:45 AM
 *
 * @author brian
 */
public class UpdateUserEmailAddressTask extends UpdateProfileAccountConfirmationBaseTask<Object> {
    private final String emailAddress;

    public UpdateUserEmailAddressTask(User user, UpdateEmailAddressInput input) {
        super(user, input);
        this.emailAddress = input.getEmailAddress();
    }

    @Override
    protected void validate(ValidationContext validationContext) {
        super.validate(validationContext);

        if (!MailUtil.isEmailAddressValid(emailAddress)) {
            validationContext.addInvalidFieldError(UpdateEmailAddressInput.Fields.emailAddress);
            return;
        }

        // make sure an account doesn't already exist with this email
        EmailAddress existingEmail = EmailAddress.dao().getByEmailAddress(emailAddress);
        if (exists(existingEmail)) {
            if (isEqual(getUser(), existingEmail.getUser())) {
                validationContext.addFieldError(UpdateEmailAddressInput.Fields.emailAddress, "updateUserEmailAddressTask.youAreUsingThisEmailAddress");
            } else {
                validationContext.addFieldError(UpdateEmailAddressInput.Fields.emailAddress, "updateUserEmailAddressTask.emailAddressAlreadyInUse");
            }
        }
    }

    @Override
    protected Object doMonitoredTask() {
        getAreaContext().doAreaTask(new StartEmailChangeProcessTask(getUser(), emailAddress));
        return null;
    }
}
