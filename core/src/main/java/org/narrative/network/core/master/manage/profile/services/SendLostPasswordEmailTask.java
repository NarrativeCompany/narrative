package org.narrative.network.core.master.manage.profile.services;

import org.narrative.common.util.IPStringUtil;
import org.narrative.network.core.user.User;
import org.narrative.network.customizations.narrative.services.ReactRoute;
import org.narrative.network.shared.email.NetworkMailUtil;
import org.narrative.network.shared.tasktypes.GlobalTaskImpl;

import java.util.List;

/**
 * Date: Jan 19, 2006
 * Time: 8:38:50 AM
 *
 * @author Brian
 */
public class SendLostPasswordEmailTask extends GlobalTaskImpl<Object> {
    private static final String RESET_PASSWORD_PRIVATE_KEY = SendLostPasswordEmailTask.class.getName() + "-private-key-kzNEryNyCFAhuzi{CADy3yuXxgqnvyyvbv8;DnXucEh9rGWErDewRHTe8xEmsrc=";

    private final User user;

    public SendLostPasswordEmailTask(User user) {
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

    public boolean isForceWritable() {
        return false;
    }

    public String getResetPasswordUrlForUser() {
        long timestamp = System.currentTimeMillis();
        return ReactRoute.RESET_PASSWORD.getUrl(
                user.getOid().toString(),
                Long.toString(timestamp),
                getResetPasswordKeyForUser(timestamp));
    }

    public String getResetPasswordKeyForUser(Long timestamp) {
        return getResetPasswordKeyForUser(user, timestamp);
    }

    public static String getResetPasswordKeyForUser(User user, Long timestamp) {
        List<Object> objectsForKey = user.getObjectsForSecurityToken();
        if (timestamp != null) {
            objectsForKey.add(timestamp);
        }
        objectsForKey.add(RESET_PASSWORD_PRIVATE_KEY);
        return IPStringUtil.getMD5DigestFromObjects(objectsForKey);
    }
}
