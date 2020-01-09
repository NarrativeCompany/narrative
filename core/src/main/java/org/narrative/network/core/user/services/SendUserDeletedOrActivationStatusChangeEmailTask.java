package org.narrative.network.core.user.services;

import org.narrative.network.core.user.User;
import org.narrative.network.shared.email.NetworkMailUtil;

/**
 * Date: 11/6/17
 * Time: 1:00 PM
 *
 * @author martin
 */
public class SendUserDeletedOrActivationStatusChangeEmailTask extends UserAccountStatusChangeEventTaskBase {
    public SendUserDeletedOrActivationStatusChangeEmailTask(User user, String originalName, String originalEmail, boolean deleteAccountContent) {
        super(user, originalName, originalEmail, deleteAccountContent);
    }

    protected Object doMonitoredTask() {
        NetworkMailUtil.sendJspCreatedEmail(this, getUser(), null, getOriginalEmail(), null, getOriginalName());
        return null;
    }
}
