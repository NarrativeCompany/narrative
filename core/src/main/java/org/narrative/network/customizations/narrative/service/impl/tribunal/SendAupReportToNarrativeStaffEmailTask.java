package org.narrative.network.customizations.narrative.service.impl.tribunal;

import org.narrative.common.util.posting.MessageTextMassager;
import org.narrative.network.core.rating.Ratable;
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
public class SendAupReportToNarrativeStaffEmailTask extends SendMultiNarrativeEmailTaskBase {
    private final User reporter;
    private final Ratable ratable;
    private final String reason;

    private List<User> users;

    public SendAupReportToNarrativeStaffEmailTask(User reporter, Ratable ratable, String reason) {
        this.reporter = reporter;
        this.ratable = ratable;
        this.reason = MessageTextMassager.getMassagedTextForBasicTextarea(reason, true);
    }

    @Override
    protected Object doMonitoredTask() {
        users = User.dao().getAllUsersWithCommunityRights(getAreaContext().getArea(), GlobalSecurable.REMOVE_AUP_VIOLATIONS);

        return super.doMonitoredTask();
    }

    public User getReporter() {
        return reporter;
    }

    public Ratable getRatable() {
        return ratable;
    }

    public String getReason() {
        return reason;
    }

    @Override
    protected List<User> getUsers() {
        return users;
    }
}
