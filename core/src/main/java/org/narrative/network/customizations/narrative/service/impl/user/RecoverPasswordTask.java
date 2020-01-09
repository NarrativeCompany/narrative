package org.narrative.network.customizations.narrative.service.impl.user;

import org.narrative.common.util.IPStringUtil;
import org.narrative.common.util.MailUtil;
import org.narrative.network.core.master.manage.profile.services.SendLostPasswordEmailTask;
import org.narrative.network.core.user.User;
import org.narrative.network.core.user.services.RecaptchaValidation;
import org.narrative.network.customizations.narrative.service.api.ValidationContext;
import org.narrative.network.customizations.narrative.service.api.model.input.RecoverPasswordInput;
import org.narrative.network.shared.security.AccessViolation;
import org.narrative.network.shared.tasktypes.GlobalTaskImpl;

import static org.narrative.common.util.CoreUtils.*;
import static org.narrative.network.customizations.narrative.controller.UserController.*;

public class RecoverPasswordTask extends GlobalTaskImpl<Object> {

    private final RecoverPasswordInput input;

    private User user;

    RecoverPasswordTask(RecoverPasswordInput input) {
        this.input = input;
    }

    @Override
    protected void validate(ValidationContext validationContext) {
        // bl: first and foremost, must have a valid reCAPTCHA
        if(RecaptchaValidation.validateRecaptcha(input.getRecaptchaResponse(), getNetworkContext(), validationContext)) {
            String emailAddress = input.getEmailAddress();
            if (IPStringUtil.isEmpty(emailAddress)) {
                validationContext.addRequiredFieldError(EMAIL_ADDRESS_FIELD_NAME);
            } else if (!MailUtil.isEmailAddressValid(emailAddress)) {
                validationContext.addInvalidFieldError(EMAIL_ADDRESS_FIELD_NAME);
            } else {
                // jw: since this is account recovery we should only take the active email address into consideration.
                user = User.dao().getByPrimaryEmailAddress(emailAddress);
            }
        }
    }

    @Override
    protected Object doMonitoredTask() {
        if (exists(user)) {
            try {
                // send the confirmation email
                getNetworkContext().doGlobalTask(new SendLostPasswordEmailTask(user));
            } catch (AccessViolation e) {
                throwUnexpectedError("checkGeneralSiteAccess threw an AccessViolation");
            }
        }
        return null;
    }
}
