package org.narrative.network.customizations.narrative.niches.services;

import org.narrative.network.core.user.User;

import java.util.Collection;

/**
 * User: brian
 * Date: 3/14/19
 * Time: 9:20 PM
 */
public abstract class SendMultiNarrativeEmailTaskBase extends SendNarrativeEmailTaskBase {

    protected abstract Collection<User> getUsers();

    @Override
    protected Object doMonitoredTask() {
        for (User user : getUsers()) {
            sendEmailForUser(user);
        }
        return null;
    }
}
