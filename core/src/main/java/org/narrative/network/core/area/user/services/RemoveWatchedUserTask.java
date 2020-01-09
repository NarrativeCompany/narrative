package org.narrative.network.core.area.user.services;

import org.narrative.network.core.area.user.AreaUser;
import org.narrative.network.core.area.user.WatchedUser;
import org.narrative.network.shared.tasktypes.AreaTaskImpl;

/**
 * Date: 7/16/15
 * Time: 6:41 PM
 *
 * @author brian
 */
public class RemoveWatchedUserTask extends AreaTaskImpl<Object> {
    private final WatchedUser watchedUser;

    public RemoveWatchedUserTask(WatchedUser watchedUser) {
        this.watchedUser = watchedUser;
    }

    @Override
    protected Object doMonitoredTask() {
        boolean wasBlockingUser = watchedUser.isBlocked();
        AreaUser followingAreaUser = AreaUser.dao().lock(watchedUser.getWatchedUser().getLoneAreaUser());
        WatchedUser.dao().delete(watchedUser);

        if (!wasBlockingUser) {
            followingAreaUser.setFollowerCount(Math.max(0, followingAreaUser.getFollowerCount() - 1));
        }
        return null;
    }
}
