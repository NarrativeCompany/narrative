package org.narrative.network.core.area.base.services;

import org.narrative.network.core.area.base.Area;
import org.narrative.network.core.area.base.AreaStats;
import org.narrative.network.core.area.user.AreaUser;
import org.narrative.network.core.area.user.AreaUserRlm;
import org.narrative.network.core.security.area.community.advanced.AreaCircle;
import org.narrative.network.core.user.User;
import org.narrative.network.shared.tasktypes.AreaTaskImpl;

import java.sql.Timestamp;
import java.util.Collection;

import static org.narrative.common.util.CoreUtils.*;
import static org.narrative.network.shared.util.NetworkCoreUtils.*;

/**
 * Created by IntelliJ IDEA.
 * User: Paul
 * Date: Jan 26, 2006
 * Time: 10:09:11 PM
 */
public class JoinGroupTask extends AreaTaskImpl<AreaUser> {
    private final User user;
    private boolean bypassModeration;

    public JoinGroupTask(User user) {
        this.user = user;
    }

    protected AreaUser doMonitoredTask() {
        Collection<AreaCircle> invitedToAreaCircles = newHashSet();

        //where they invited by an admin?
        Area area = getAreaContext().getArea();

        AreaUser areaUser = AreaUser.dao().getAreaUserFromUserAndArea(user.getOid(), area.getOid());
        AreaUserRlm areaUserRlm;

        if (!exists(areaUser)) {
            areaUser = new AreaUser(area, user);

            AreaUser.dao().save(areaUser);
            //create the area user rlm
            areaUserRlm = new AreaUserRlm(getAreaRlm(area), areaUser);

            AreaUserRlm.dao().save(areaUserRlm);

        } else {
            // bl: since we are going to be making changes to the AreaUser object now, we need to lock it up front
            // since we know we will need to lock it eventually (for adding points for joining the group).
            areaUser = AreaUser.dao().lock(areaUser);
            // bl: set the createdDatetime to the time that the user joined the group
            areaUser.setCreatedDatetime(new Timestamp(System.currentTimeMillis()));
        }

        for (AreaCircle group : invitedToAreaCircles) {
            areaUser.addToAreaCircle(group);
        }

        {
            AreaStats areaStats = AreaStats.dao().getLocked(area.getOid());

            areaStats.updateMemberCount();
        }

        return areaUser;
    }

    public boolean isBypassModeration() {
        return bypassModeration;
    }

    public void setBypassModeration(boolean bypassModeration) {
        this.bypassModeration = bypassModeration;
    }

}

