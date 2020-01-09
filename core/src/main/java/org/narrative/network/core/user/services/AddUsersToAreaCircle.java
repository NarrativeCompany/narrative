package org.narrative.network.core.user.services;

import org.narrative.network.core.area.user.AreaUser;
import org.narrative.network.core.security.area.community.advanced.AreaCircle;
import org.narrative.network.core.user.User;

import java.util.Collection;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Date: 6/7/13
 * Time: 10:36 AM
 *
 * @author brian
 */
public class AddUsersToAreaCircle extends PermissionCircleChangeTaskBase {
    public AddUsersToAreaCircle(AreaCircle circle, Collection<User> users) {
        super(circle, users);
    }

    @Override
    protected Object doMonitoredTask() {
        for (User user : getUsers()) {
            AreaUser areaUser = user.getAreaUserByArea(getAreaContext().getArea());
            // jw: only process the user if they exist, are visible, and were not already in the circle
            if (exists(areaUser) && areaUser.getUser().isVisible()) {
                if (!areaUser.getAreaCircleUsersInited().containsKey(getCircle())) {
                    areaUser.addToAreaCircle(getCircle());
                }
            }
        }
        return null;
    }

}
