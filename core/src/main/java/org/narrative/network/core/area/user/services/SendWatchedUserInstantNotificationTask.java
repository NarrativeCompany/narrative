package org.narrative.network.core.area.user.services;

import org.narrative.network.core.user.User;
import org.narrative.network.core.user.services.MobilePushNotificationTask;
import org.narrative.network.shared.tasktypes.AreaTaskImpl;

import static org.narrative.network.shared.util.NetworkCoreUtils.*;

/**
 * Date: 7/11/12
 * Time: 3:36 PM
 * User: jonmark
 */
public class SendWatchedUserInstantNotificationTask extends AreaTaskImpl<Object> implements MobilePushNotificationTask {
    private final User watchedUser;
    private final User watchingUser;

    public SendWatchedUserInstantNotificationTask(User watchedUser, User watchingUser) {
        super(false);
        this.watchedUser = watchedUser;
        this.watchingUser = watchingUser;
    }

    @Override
    protected Object doMonitoredTask() {
        // jw: this process will only send to instant notification channels that the member has enabled (so if they turned off their emails, it wont be sent via email).
        watchedUser.sendInstantNotification(this);
        return null;
    }

    public User getWatchedUser() {
        return watchedUser;
    }

    public User getWatchingUser() {
        return watchingUser;
    }

    @Override
    public String getNotificationMessage() {
        return wordlet("jsp.site.email.emailWatchedUser.youHaveANewFollower");
    }

    @Override
    public String getTargetUrl() {
        return getWatchingUser().getPermalinkUrl();
    }

}
