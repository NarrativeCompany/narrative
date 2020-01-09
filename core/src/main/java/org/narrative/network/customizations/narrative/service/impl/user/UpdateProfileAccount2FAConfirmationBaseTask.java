package org.narrative.network.customizations.narrative.service.impl.user;

import org.narrative.network.core.user.User;
import org.narrative.network.core.user.services.CheckTwoFactorAuthenticationBackupCodeTask;
import org.narrative.network.core.user.services.TwoFactorAuthUtils;
import org.narrative.network.customizations.narrative.service.api.ValidationContext;
import org.narrative.network.customizations.narrative.service.api.model.input.UpdateProfileAccount2FAConfirmationInputBase;
import org.narrative.network.customizations.narrative.service.api.model.input.UpdateProfileAccountConfirmationInputBase;
import org.narrative.network.shared.tasktypes.AreaTaskImpl;

/**
 * Date: 2019-07-08
 * Time: 15:08
 *
 * @author jonmark
 */
public abstract class UpdateProfileAccount2FAConfirmationBaseTask<T> extends AreaTaskImpl<T> {
    private final User user;
    private final Integer twoFactorAuthCode;

    public UpdateProfileAccount2FAConfirmationBaseTask(User user, UpdateProfileAccount2FAConfirmationInputBase input) {
        this.user = user;
        this.twoFactorAuthCode = input.getTwoFactorAuthCode();
    }

    @Override
    protected void validate(ValidationContext validationContext) {
        if(user.isTwoFactorAuthenticationEnabled()) {
            assert twoFactorAuthCode != null : "We should always be provided a 2FA code!";

            if(!TwoFactorAuthUtils.isValidCode(user.getTwoFactorAuthenticationSecretKey(), twoFactorAuthCode)) {
                // jw: finally, since the code did not match what is currently being generated, we need to see if this
                //     is one of the users backup codes.
                if (!getAreaContext().doAreaTask(new CheckTwoFactorAuthenticationBackupCodeTask(user, twoFactorAuthCode))) {
                    validationContext.addInvalidFieldError(UpdateProfileAccountConfirmationInputBase.Fields.twoFactorAuthCode);
                }
            }
        }
    }

    protected User getUser() {
        return user;
    }
}
