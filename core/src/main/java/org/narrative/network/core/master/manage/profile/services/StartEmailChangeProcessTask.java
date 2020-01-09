package org.narrative.network.core.master.manage.profile.services;

import org.narrative.common.util.UnexpectedError;
import org.narrative.network.core.user.EmailAddress;
import org.narrative.network.core.user.User;
import org.narrative.network.shared.tasktypes.AreaTaskImpl;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Date: 2019-07-10
 * Time: 11:12
 *
 * @author jonmark
 */
public class StartEmailChangeProcessTask extends AreaTaskImpl<Object> {
    private final User user;
    private final String newEmailAddress;

    public StartEmailChangeProcessTask(User user, String newEmailAddress) {
        this.user = user;
        this.newEmailAddress = newEmailAddress;
    }

    @Override
    protected Object doMonitoredTask() {
        EmailAddress pendingEmailAddress = user.getUserFields().getPendingEmailAddress();
        if (exists(pendingEmailAddress)) {
            throw UnexpectedError.getRuntimeException("Should never try to change the email address when a email address change is already in progress!");
        }

        // jw: first things first, create the new pending email address
        pendingEmailAddress = new EmailAddress(user, newEmailAddress);

        // jw: we can save it right away!
        EmailAddress.dao().save(pendingEmailAddress);

        // jw: finally, send an email for the user to verify both email addresses
        getNetworkContext().doGlobalTask(new SendEmailChangedEmailTask(user));

        return null;
    }
}
