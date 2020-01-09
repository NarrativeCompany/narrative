package org.narrative.network.customizations.narrative.service.impl.user;

import org.narrative.common.util.MailUtil;
import org.narrative.network.core.user.EmailAddress;
import org.narrative.network.core.user.User;
import org.narrative.network.core.user.services.CreateNewUser;
import org.narrative.network.core.user.services.SendNewUserEmailTask;
import org.narrative.network.core.user.services.preferences.FormatPreferences;
import org.narrative.network.customizations.narrative.channels.channel.services.UpdateFollowedChannelTask;
import org.narrative.network.customizations.narrative.niches.niche.Niche;
import org.narrative.network.customizations.narrative.service.api.ValidationContext;
import org.narrative.network.customizations.narrative.service.api.model.input.RegisterUserInput;
import org.narrative.network.shared.context.NetworkContextImplBase;
import org.narrative.network.shared.tasktypes.GlobalTaskImpl;

import java.util.List;

import static org.narrative.common.util.CoreUtils.*;
import static org.narrative.network.shared.util.NetworkCoreUtils.*;

/**
 * Date: 9/26/18
 * Time: 8:49 AM
 *
 * @author brian
 */
public class RegisterUserTask extends GlobalTaskImpl<User> {
    public static final int MIN_REQUIRED_NICHES_TO_FOLLOW = 3;

    private final RegisterUserInput registerInput;
    private List<Niche> nichesToFollow;

    public RegisterUserTask(RegisterUserInput registerInput) {
        this.registerInput = registerInput;
    }

    @Override
    protected void validate(ValidationContext validationContext) {

        validateEmailAddress(registerInput.getEmailAddress(), RegisterUserInput.Fields.emailAddress, null, validationContext);

        // validate the username and display name
        UpdateUserProfileTask.validate(validationContext, null, registerInput);

        this.nichesToFollow = Niche.dao().getObjectsFromIDsWithCache(registerInput.getNichesToFollow());

        // Validate that registerInput.getNichesToFollow is not null and has at least MIN_REQUIRED_NICHES_TO_FOLLOW items
        if (nichesToFollow == null || nichesToFollow.size() < MIN_REQUIRED_NICHES_TO_FOLLOW) {
            validationContext.addMethodError(wordlet("register.notEnoughNichesOfInterest", MIN_REQUIRED_NICHES_TO_FOLLOW));
        } else {
            // Validate that the niches are active
            nichesToFollow.forEach(niche -> {
                if (!niche.getStatus().isActive()) {
                    validationContext.addMethodError(wordlet("narrativePostFields.specifiedNonActiveNiche", niche.getName()));
                }
            });
        }

        // bl: since the reCAPTCHA is on step one of the form, we have to validate it during step one validation
        // submission and use this token to re-verify the captcha is correct.
        String expectedRecaptchaToken = ValidateRegisterUserStepOneTask.getRecaptchaTokenForDetails(registerInput);
        if(!isEqual(expectedRecaptchaToken, registerInput.getRecaptchaToken())) {
            validationContext.addMethodError(wordlet("reCaptcha.error"));
        }
    }

    public static boolean validateEmailAddress(String emailAddress, String fieldName, User user, ValidationContext validationContext) {
        if (!MailUtil.isEmailAddressValid(emailAddress)) {
            validationContext.addInvalidFieldError(fieldName);
            return false;
        }

        // make sure an account doesn't already exist with this email
        EmailAddress existingEmail = EmailAddress.dao().getByEmailAddress(emailAddress);
        if (exists(existingEmail) && !isEqual(user, existingEmail.getUser())) {
            validationContext.addFieldError(fieldName, "emailAddressAlreadyRegisteredError");
            return false;
        }

        return true;
    }

    @Override
    protected User doMonitoredTask() {
        CreateNewUser createNewUser = new CreateNewUser(registerInput.getPassword(), registerInput.getDisplayName(), registerInput.getUsername(), registerInput.getEmailAddress(), false);
        FormatPreferences formatPreferences = getNetworkContext().getFormatPreferences();
        formatPreferences.setTimeZone(registerInput.getTimeZone());
        createNewUser.setFormatPreferences(formatPreferences);

        User user = getNetworkContext().doGlobalTask(createNewUser);

        // bl: we know the user agreed to the TOS
        user.getUserFields().setHasUserAgreedToTos(true);

        sendNewUserEmail(user);

        // Set the current user
        ((NetworkContextImplBase) getNetworkContext()).setPrimaryRole(user);

        // Set nichesToFollow to Watched
        assert nichesToFollow != null : "nichesToFollow must not be null";
        nichesToFollow.forEach(niche -> getNetworkContext().doGlobalTask(new UpdateFollowedChannelTask(niche.getChannel(), true)));

        return user;
    }

    public static void sendNewUserEmail(User newUser) {
        networkContext().doGlobalTask(new SendNewUserEmailTask(newUser));
    }
}
