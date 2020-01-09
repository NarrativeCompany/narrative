package org.narrative.network.core.master.manage.profile.services;

import org.narrative.network.core.user.Credentials;
import org.narrative.network.core.user.User;
import org.narrative.network.shared.email.NetworkMailUtil;
import org.narrative.network.shared.tasktypes.GlobalTaskImpl;

/**
 * Date: 6/9/14
 * Time: 3:10 PM
 *
 * @author brian
 */
public class ChangePasswordTask extends GlobalTaskImpl<Object> {

    private final User user;
    private final String password;

    public ChangePasswordTask(User user, String password) {
        this.user = user;
        this.password = password;
    }

    @Override
    protected Object doMonitoredTask() {
        if (!user.isHasInternalCredentials()) {
            Credentials credentials = user.getAuthZone().getAndSaveNewCredentials(user.getEmailAddress(), password);
            user.addUserAuth(user.getAuthZone().getInternalAuthProvider(), credentials.getOid().toString());
        } else {
            user.getInternalCredentials().getPasswordFields().setPassword(password);
        }
        NetworkMailUtil.sendJspCreatedEmail(this, user);
        return null;
    }

    public User getUser() {
        return user;
    }
}
