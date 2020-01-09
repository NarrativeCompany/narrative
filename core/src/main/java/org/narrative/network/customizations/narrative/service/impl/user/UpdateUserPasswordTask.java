package org.narrative.network.customizations.narrative.service.impl.user;

import org.narrative.network.core.master.manage.profile.services.ChangePasswordTask;
import org.narrative.network.core.user.User;
import org.narrative.network.customizations.narrative.service.api.model.TokenDTO;
import org.narrative.network.customizations.narrative.service.api.model.input.UpdatePasswordInput;

/**
 * Date: 9/28/18
 * Time: 7:45 AM
 *
 * @author brian
 */
public class UpdateUserPasswordTask extends UpdateProfileAndJwtBaseTask {
    private final String newPassword;

    public UpdateUserPasswordTask(User user, UpdatePasswordInput updatePassword) {
        super(user, updatePassword);
        this.newPassword = updatePassword.getNewPassword();
    }

    @Override
    protected TokenDTO doMonitoredTask() {
        getNetworkContext().doGlobalTask(new ChangePasswordTask(getUser(), newPassword));
        return super.doMonitoredTask();
    }
}
