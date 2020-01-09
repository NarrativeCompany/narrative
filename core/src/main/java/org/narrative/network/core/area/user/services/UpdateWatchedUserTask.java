package org.narrative.network.core.area.user.services;

import org.narrative.network.core.area.user.AreaUser;
import org.narrative.network.core.area.user.WatchedUser;
import org.narrative.network.core.system.NetworkRegistry;
import org.narrative.network.core.user.User;
import org.narrative.network.shared.tasktypes.AreaTaskImpl;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Date: 10/7/12
 * Time: 4:21 PM
 * User: jonmark
 */
public class UpdateWatchedUserTask extends AreaTaskImpl<WatchedUser> {
    private final User userToWatch;
    private final User userWatching;
    private WatchedUser watchedUser;
    private final boolean blockUser;

    public UpdateWatchedUserTask(User userToWatch, User userWatching, WatchedUser watchedUser, boolean blockUser) {
        this.userToWatch = userToWatch;
        this.userWatching = userWatching;
        this.watchedUser = watchedUser;
        this.blockUser = blockUser;
    }

    @Override
    protected WatchedUser doMonitoredTask() {
        if (exists(watchedUser)) {
            watchedUser.setBlocked(blockUser);

            // jw: its possible that a user could follow someone, and then un-follow them, and then follow them again, but
            //     lets not concern ourselves with that for now.
            if (blockUser && !watchedUser.isBlocked()) {
                // if the person used to be following this use we need to increment their follow count
                watchedUser.setBlocked(true);
                AreaUser followingAreaUser = AreaUser.dao().lock(userToWatch.getLoneAreaUser());
                followingAreaUser.setFollowerCount(followingAreaUser.getFollowerCount() - 1);
            }

        } else {
            AreaUser followingAreaUser = AreaUser.dao().lock(userToWatch.getLoneAreaUser());

            watchedUser = new WatchedUser(userWatching, userToWatch);
            watchedUser.setBlocked(blockUser);
            WatchedUser.dao().save(watchedUser);

            if (!blockUser) {
                followingAreaUser.setFollowerCount(followingAreaUser.getFollowerCount() + 1);

                // bl: only record activity stream items and send alerts/emails when user following is enabled.
                // otherwise, this is an admin performing the action, so allow it to happen in secret.
                if (!NetworkRegistry.getInstance().isImporting()) {
                    if (userToWatch.getLoneAreaUser().isWatchForSomeoneFollowingMe()) {
                        getAreaContext().doAreaTask(new SendWatchedUserInstantNotificationTask(userToWatch, userWatching));
                    }
                }
            }
        }

        return watchedUser;
    }
}
