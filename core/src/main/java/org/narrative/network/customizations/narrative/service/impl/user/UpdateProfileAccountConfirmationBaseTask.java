package org.narrative.network.customizations.narrative.service.impl.user;

import org.narrative.common.util.IPStringUtil;
import org.narrative.network.core.user.User;
import org.narrative.network.customizations.narrative.service.api.ValidationContext;
import org.narrative.network.customizations.narrative.service.api.model.input.UpdateProfileAccountConfirmationInputBase;

/**
 * Date: 9/28/18
 * Time: 7:59 AM
 *
 * @author brian
 */
public abstract class UpdateProfileAccountConfirmationBaseTask<T> extends UpdateProfileAccount2FAConfirmationBaseTask<T> {
    private final String currentPassword;

    public UpdateProfileAccountConfirmationBaseTask(User user, UpdateProfileAccountConfirmationInputBase input) {
        super(user, input);
        this.currentPassword = input.getCurrentPassword();
    }

    @Override
    protected void validate(ValidationContext validationContext) {
        if (IPStringUtil.isEmpty(currentPassword)) {
            validationContext.addRequiredFieldError(UpdateProfileAccountConfirmationInputBase.Fields.currentPassword);
        } else if (!getUser().getInternalCredentials().getPasswordFields().isCorrectPassword(currentPassword)) {
            validationContext.addFieldError(UpdateProfileAccountConfirmationInputBase.Fields.currentPassword, "updateProfilePasswordConfirmationBaseTask.incorrectPassword");
        }

        super.validate(validationContext);
    }
}
