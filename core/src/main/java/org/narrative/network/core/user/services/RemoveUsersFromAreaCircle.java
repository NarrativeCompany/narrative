package org.narrative.network.core.user.services;

import org.narrative.network.core.area.user.AreaUser;
import org.narrative.network.core.security.area.community.advanced.AreaCircle;
import org.narrative.network.core.user.User;

import java.util.Collection;
import java.util.List;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Date: 6/7/13
 * Time: 10:40 AM
 *
 * @author brian
 */
public class RemoveUsersFromAreaCircle extends PermissionCircleChangeTaskBase {

    public RemoveUsersFromAreaCircle(AreaCircle circle, Collection<User> users) {
        super(circle, users);
    }

    @Override
    protected Object doMonitoredTask() {
        List<AreaUser> areaUsers = newArrayList(getUsers().size());
        for (User user : getUsers()) {
            AreaUser areaUser = user.getAreaUserByArea(getAreaContext().getArea());
            // jw: only add the user if they exist, are visible, and are a member of the circle
            if (exists(areaUser) && areaUser.getUser().isVisible() && areaUser.getAreaCircleUsersInited().containsKey(getCircle())) {
                areaUsers.add(areaUser);
            }
        }

        for (AreaUser areaUser : areaUsers) {
            areaUser.removeFromAreaCircle(getCircle());
        }
        return null;
    }

}
