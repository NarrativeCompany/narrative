package org.narrative.network.core.user.services;

import org.narrative.network.core.security.area.community.advanced.AreaCircle;
import org.narrative.network.core.user.User;
import org.narrative.network.shared.tasktypes.AreaTaskImpl;

import java.util.Collection;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Date: 3/19/14
 * Time: 8:02 AM
 *
 * @author brian
 */
public abstract class PermissionCircleChangeTaskBase extends AreaTaskImpl<Object> {

    private final AreaCircle circle;
    private final Collection<User> users;

    protected PermissionCircleChangeTaskBase(AreaCircle circle, Collection<User> users) {
        assert exists(circle) : "Should always specify the circle!";
        this.circle = circle;
        this.users = users;
    }

    public AreaCircle getCircle() {
        return circle;
    }

    protected Collection<User> getUsers() {
        return users;
    }
}
