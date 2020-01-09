package org.narrative.network.customizations.narrative.niches.services;

import org.narrative.network.core.user.User;

import static org.narrative.common.util.CoreUtils.*;

/**
 * User: brian
 * Date: 3/14/19
 * Time: 9:20 PM
 */
public class SendSingleNarrativeEmailTaskBase extends SendNarrativeEmailTaskBase {
    private final User user;

    public SendSingleNarrativeEmailTaskBase(User user) {
        this.user = user;
    }

    @Override
    protected Object doMonitoredTask() {
        // bl: don't do anything if the user doesn't exist
        if(exists(user)) {
            sendEmailForUser(user);
        }
        return null;
    }
}
