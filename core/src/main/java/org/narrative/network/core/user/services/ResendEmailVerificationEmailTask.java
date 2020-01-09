package org.narrative.network.core.user.services;

import org.narrative.network.core.user.User;
import org.narrative.network.shared.email.NetworkMailUtil;
import org.narrative.network.shared.tasktypes.GlobalTaskImpl;

/**
 * Date: Feb 4, 2008
 * Time: 10:15:21 AM
 *
 * @author brian
 */
public class ResendEmailVerificationEmailTask extends GlobalTaskImpl<Object> {
    private final User user;

    public ResendEmailVerificationEmailTask(User user) {
        super(false);
        this.user = user;
    }

    protected Object doMonitoredTask() {
        NetworkMailUtil.sendJspCreatedEmail(this, user);
        return null;
    }

    public User getUser() {
        return user;
    }
}
