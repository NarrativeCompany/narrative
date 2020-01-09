package org.narrative.network.core.user.services;

import org.narrative.network.core.user.User;
import org.narrative.network.core.user.UserStatus;
import org.narrative.network.shared.tasktypes.GlobalTaskImpl;

/**
 * Created by IntelliJ IDEA.
 * User: Laurence
 * Date: 7/11/2017
 * Time: 14:59
 */
public abstract class ActivateDeactivateUserTaskBase extends GlobalTaskImpl<Object> {

    private void deactivateUser(User user) {
        assert !user.isDeactivated() : "User must not be already deactivated Users can't deactivate themselves";
        user.getUserStatus().turnOn(UserStatus.DEACTIVATED);
    }

    private void reactivateUser(User user) {
        assert user.isDeactivated() : "User must be deactivated";
        user.getUserStatus().turnOff(UserStatus.DEACTIVATED);
    }

    void toggleUserStatus(User user, boolean deactivate) {
        if (deactivate) {
            deactivateUser(user);
        } else {
            reactivateUser(user);
        }
    }
}
