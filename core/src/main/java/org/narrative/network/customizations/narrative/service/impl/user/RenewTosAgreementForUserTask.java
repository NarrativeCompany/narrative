package org.narrative.network.customizations.narrative.service.impl.user;

import org.narrative.network.core.user.User;
import org.narrative.network.core.user.services.ActivateUserTask;
import org.narrative.network.core.user.services.SendUserDeletedOrActivationStatusChangeEmailTask;
import org.narrative.network.shared.tasktypes.AreaTaskImpl;

/**
 * Date: 9/29/18
 * Time: 10:32 AM
 *
 * @author brian
 */
public class RenewTosAgreementForUserTask extends AreaTaskImpl<Object> {
    private final User user;
    public RenewTosAgreementForUserTask(User user) {
        this.user = user;
    }

    @Override
    protected Object doMonitoredTask() {
        // bl: make this a no-op if the user is already re-activated
        if(user.isDeactivated()) {
            getNetworkContext().doGlobalTask(new ActivateUserTask(user));

            // jw: notify the member that their activation status has changed
            getAreaContext().doAreaTask(new SendUserDeletedOrActivationStatusChangeEmailTask(user, user.getDisplayName(), null, false));

            user.getUserFields().setHasUserAgreedToTos(true);
        }
        return null;
    }
}
