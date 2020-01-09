package org.narrative.network.customizations.narrative.service.impl.user;

import org.narrative.common.util.ApplicationError;
import org.narrative.common.util.IPDateUtil;
import org.narrative.common.util.IPUtil;
import org.narrative.common.util.UnexpectedError;
import org.narrative.common.util.ValidationHandler;
import org.narrative.network.core.master.manage.profile.services.ChangePasswordTask;
import org.narrative.network.core.master.manage.profile.services.SendLostPasswordEmailTask;
import org.narrative.network.core.user.User;
import org.narrative.network.customizations.narrative.service.api.model.input.ResetPasswordInput;
import org.narrative.network.shared.context.AreaContext;

import static org.narrative.common.util.CoreUtils.*;
import static org.narrative.network.shared.util.NetworkCoreUtils.*;

public class ResetPasswordTask extends UpdateProfileAccount2FAConfirmationBaseTask<Object> {
    private final ResetPasswordInput input;

    ResetPasswordTask(User user, ResetPasswordInput input) {
        super(user, input);

        this.input = input;
    }

    public static boolean isURLTimestampExpired(Long timestamp) {
        return timestamp == null || (timestamp < (System.currentTimeMillis() - IPDateUtil.HOUR_IN_MS));
    }

    public static void validateResetURLParams(User user, AreaContext areaContext, Long timestamp, String key) {

        if (!exists(user)) {
            throw UnexpectedError.getRuntimeException("Must supply user when doing a password reset!");
        }

        if (!isEqual(key, SendLostPasswordEmailTask.getResetPasswordKeyForUser(user, timestamp))) {
            throw UnexpectedError.getRuntimeException("Invalid reset password URL");
        }
    }

    @Override
    protected void validate(ValidationHandler validationHandler) {
        validateResetURLParams(getUser(), getAreaContext(), input.getTimestamp(), input.getResetPasswordKey());

        super.validate(validationHandler);

        // If the rest of the URL is valid, check the timestamp
        if (isURLTimestampExpired(input.getTimestamp())) {
            throw new ApplicationError(wordlet("resetPassword.urlExpired"));
        }

        // newPassword confirmation is required.  do they match?
        if (!IPUtil.isEqual(input.getPassword(), input.getPasswordConfirm())) {
            validationHandler.addFieldError("passwordConfirm", "register_updateUser.passwordsDoNotMatch");
        }
    }

    @Override
    protected Object doMonitoredTask() {
        getNetworkContext().doGlobalTask(new ChangePasswordTask(getUser(), input.getPassword()));

        return null;
    }
}
