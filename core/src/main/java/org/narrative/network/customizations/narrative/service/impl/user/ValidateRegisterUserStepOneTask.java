package org.narrative.network.customizations.narrative.service.impl.user;

import org.narrative.common.util.IPStringUtil;
import org.narrative.network.core.user.services.RecaptchaValidation;
import org.narrative.network.customizations.narrative.service.api.ValidationContext;
import org.narrative.network.customizations.narrative.service.api.model.input.RegisterUserStepOneInput;
import org.narrative.network.shared.tasktypes.GlobalTaskImpl;

/**
 * Date: 2019-02-18
 * Time: 10:37
 *
 * @author brian
 */
public class ValidateRegisterUserStepOneTask extends GlobalTaskImpl<String> {
    private static final String VALIDATE_RECAPTCHA_PRIVATE_KEY = ValidateRegisterUserStepOneTask.class.getName() + "-reCAPTCHA-private-key-mwF3o7tMjw6KWQebprNgQU[FVdpU7BzXJcBLxMkQFkKu$QBdzJ]bhbirGrAbeBhm";

    private final RegisterUserStepOneInput stepOneInput;

    public ValidateRegisterUserStepOneTask(RegisterUserStepOneInput stepOneInput) {
        this.stepOneInput = stepOneInput;
    }

    @Override
    protected void validate(ValidationContext validationContext) {
        // validate the user's email address
        RegisterUserTask.validateEmailAddress(stepOneInput.getEmailAddress(), RegisterUserStepOneInput.Fields.emailAddress, null, validationContext);

        // validate the username and display name
        UpdateUserProfileTask.validate(validationContext, null, stepOneInput);

        RecaptchaValidation.validateRecaptcha(stepOneInput.getRecaptchaResponse(), getNetworkContext(), validationContext);
    }

    @Override
    protected String doMonitoredTask() {
        // just validating here, so nothing else to do other than to return the token
        return getRecaptchaTokenForDetails(stepOneInput);
    }

    public static String getRecaptchaTokenForDetails(RegisterUserStepOneInput stepOneInput) {
        return IPStringUtil.getMD5DigestFromObjects(stepOneInput.getEmailAddress(), stepOneInput.getUsername(), stepOneInput.getDisplayName(), stepOneInput.getRecaptchaResponse(), VALIDATE_RECAPTCHA_PRIVATE_KEY);
    }
}
