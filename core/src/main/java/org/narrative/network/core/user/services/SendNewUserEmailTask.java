package org.narrative.network.core.user.services;

import org.narrative.network.core.user.User;
import org.narrative.network.shared.email.NetworkMailUtil;
import org.narrative.network.shared.tasktypes.GlobalTaskImpl;

/**
 * Created by IntelliJ IDEA.
 * User: barry
 * Date: Jun 12, 2006
 * Time: 4:46:46 PM
 */
public class SendNewUserEmailTask extends GlobalTaskImpl<Object> {
    private final User user;

    public SendNewUserEmailTask(User user) {
        super(false);
        this.user = user;
    }

    protected Object doMonitoredTask() {
        final SendNewUserEmailTask thisRef = this;
        return getNetworkContext().doAuthZoneTask(getNetworkContext().getAuthZone(), new GlobalTaskImpl<Object>(false) {
            @Override
            protected Object doMonitoredTask() {
                NetworkMailUtil.sendJspCreatedEmail(thisRef, user);
                return null;
            }
        });
    }

    public User getUser() {
        return user;
    }
}