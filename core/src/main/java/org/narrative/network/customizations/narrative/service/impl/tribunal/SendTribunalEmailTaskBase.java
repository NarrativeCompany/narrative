package org.narrative.network.customizations.narrative.service.impl.tribunal;

import org.narrative.network.core.security.area.community.advanced.services.GlobalSecurable;
import org.narrative.network.core.user.User;
import org.narrative.network.customizations.narrative.niches.services.SendMultiNarrativeEmailTaskBase;

import java.util.List;

/**
 * Date: 2019-03-16
 * Time: 17:38
 *
 * @author brian
 */
public class SendTribunalEmailTaskBase extends SendMultiNarrativeEmailTaskBase {
    private List<User> users;

    @Override
    protected Object doMonitoredTask() {
        users = User.dao().getAllUsersWithCommunityRights(getAreaContext().getArea(), GlobalSecurable.PARTICIPATE_IN_TRIBUNAL_ACTIONS);

        return super.doMonitoredTask();
    }

    @Override
    protected List<User> getUsers() {
        return users;
    }
}
