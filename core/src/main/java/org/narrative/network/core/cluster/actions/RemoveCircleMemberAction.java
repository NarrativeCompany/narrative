package org.narrative.network.core.cluster.actions;

import org.narrative.common.util.UnexpectedError;
import org.narrative.common.web.HttpMethodType;
import org.narrative.common.web.struts.MethodDetails;
import org.narrative.network.core.area.base.Area;
import org.narrative.network.core.user.User;
import org.narrative.network.core.user.services.RemoveUsersFromAreaCircle;
import org.narrative.network.shared.services.ConfirmationMessage;
import org.narrative.network.shared.struts.NetworkResponses;

import java.util.Collections;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Date: 2020-01-03
 * Time: 13:04
 *
 * @author jonmark
 */
public class RemoveCircleMemberAction extends ManageCircleMembersActionBase {
    public static final String ACTION_NAME = "remove-circle-member";
    public static final String FULL_ACTION_PATH = "/"+ ACTION_NAME;

    public static final String USER_PARAM = "user";

    private User user;

    @Override
    public void validate() {
        if (!exists(user)) {
            throw UnexpectedError.getRuntimeException("Should always have a user when we get here unless someone hacked the page!");
        }
    }

    @Override
    @MethodDetails(httpMethodType = HttpMethodType.POST)
    public String execute() throws Exception {
        getNetworkContext().doAreaTask(Area.dao().getNarrativePlatformArea(), new RemoveUsersFromAreaCircle(getAreaCircle(), Collections.singleton(user)));

        setConfirmationMessageForSubsequentRequest(new ConfirmationMessage("Member Removed", true));
        return NetworkResponses.redirectResponse();
    }

    public void setUser(User user) {
        this.user = user;
    }
}
