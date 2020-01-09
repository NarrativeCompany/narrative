package org.narrative.network.customizations.narrative.service.impl.user;

import org.narrative.common.util.IPStringUtil;
import org.narrative.network.core.user.NarrativeAuthZoneMaster;
import org.narrative.network.core.user.User;
import org.narrative.network.core.user.services.UsernameUtils;
import org.narrative.network.customizations.narrative.service.api.ValidationContext;
import org.narrative.network.customizations.narrative.service.api.model.input.UpdateUserProfileInput;
import org.narrative.network.customizations.narrative.service.api.model.input.UserProfileInputBase;
import org.narrative.network.shared.tasktypes.GlobalTaskImpl;

import static org.narrative.common.util.CoreUtils.*;
import static org.narrative.network.shared.util.NetworkCoreUtils.*;

/**
 * Date: 9/25/18
 * Time: 10:42 AM
 *
 * @author brian
 */
public class UpdateUserProfileTask extends GlobalTaskImpl<User> {

    private static final String RESERVED_NARRATIVE_NAME = NarrativeAuthZoneMaster.NARRATIVE_NAME;

    private final User user;
    private final UpdateUserProfileInput updateProfileInput;

    public UpdateUserProfileTask(User user, UpdateUserProfileInput updateProfileInput) {
        this.user = user;
        this.updateProfileInput = updateProfileInput;
    }

    @Override
    protected void validate(ValidationContext validationContext) {
        validate(validationContext, user, updateProfileInput);
    }

    public static void validate(ValidationContext validationContext, User user, UserProfileInputBase updateRequest) {
        if (UsernameUtils.INVALID_USERNAME_CHARS_PATTERN.matcher(updateRequest.getUsername()).find()) {
            validationContext.addFieldError(UpdateUserProfileInput.Fields.username, "username.requiredCharacters");
        } else {
            User userWithUsername = User.dao().getUserByUsername(networkContext().getAuthZone(), updateRequest.getUsername());
            if (exists(userWithUsername) && !isEqual(user, userWithUsername)) {
                validationContext.addFieldError(UpdateUserProfileInput.Fields.username, "name.notAvailable");
            } else {
                // bl: be sure the username is not "narrative"
                if(IPStringUtil.isStringEqualIgnoreCase(updateRequest.getUsername(), RESERVED_NARRATIVE_NAME)) {
                    validationContext.addFieldError(UpdateUserProfileInput.Fields.username, "name.notAvailable");
                }
            }
        }

        // bl: be sure the display name is not Narrative
        if (IPStringUtil.isStringEqualIgnoreCase(updateRequest.getDisplayName(), RESERVED_NARRATIVE_NAME)) {
            validationContext.addFieldError(UpdateUserProfileInput.Fields.displayName, "name.notAvailable");
        }
    }

    @Override
    protected User doMonitoredTask() {
        user.updateDisplayName(updateProfileInput.getDisplayName());
        user.setUsername(updateProfileInput.getUsername());
        return user;
    }
}
