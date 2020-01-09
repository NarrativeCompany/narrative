package org.narrative.network.core.user.services;

import org.narrative.network.core.user.User;

/**
 * Created by IntelliJ IDEA.
 * User: Laurence
 * Date: 7/11/2017
 * Time: 14:46
 */
public class ActivateUserTask extends ActivateDeactivateUserTaskBase {
    private final User user;

    public ActivateUserTask(User user) {
        this.user = user;
    }

    protected Object doMonitoredTask() {
        toggleUserStatus(user, false);
        return null;
    }

}
