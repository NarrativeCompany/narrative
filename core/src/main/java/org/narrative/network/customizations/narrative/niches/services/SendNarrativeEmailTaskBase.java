package org.narrative.network.customizations.narrative.niches.services;

import org.narrative.network.core.user.User;
import org.narrative.network.shared.email.NetworkMailUtil;
import org.narrative.network.shared.tasktypes.AreaTaskImpl;

/**
 * User: brian
 * Date: 3/14/19
 * Time: 9:16 PM
 */
public abstract class SendNarrativeEmailTaskBase extends AreaTaskImpl<Object> {
    private User user;

    public SendNarrativeEmailTaskBase() {
        super(false);
    }

    protected void sendEmailForUser(User user) {
        this.user = user;

        if (isAlwaysSendEmail() || !getUser().isSuspendAllEmails()) {
            NetworkMailUtil.sendJspCreatedEmail(this, getUser());
        }
    }

    public boolean isAlwaysSendEmail() {
        return false;
    }

    public User getUser() {
        return user;
    }
}
