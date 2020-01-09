package org.narrative.network.core.cluster.actions;

import org.narrative.common.web.HttpMethodType;
import org.narrative.common.web.struts.MethodDetails;
import org.narrative.network.core.area.base.Area;
import org.narrative.network.core.user.User;
import org.narrative.network.core.user.services.AddUsersToAreaCircle;
import org.narrative.network.shared.services.ConfirmationMessage;
import org.narrative.network.shared.struts.NetworkResponses;

import java.util.Collections;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Date: 2020-01-03
 * Time: 12:53
 *
 * @author jonmark
 */
public class AddCircleMemberAction extends ManageCircleMembersActionBase {
    public static final String ACTION_NAME = "add-circle-member";
    public static final String FULL_ACTION_PATH = "/"+ ACTION_NAME;

    public static final String HANDLE_PARAM = "handle";

    private String handle;
    private User user;

    @Override
    public void validate() {
        user = User.dao().getUserByUsername(Area.dao().getNarrativePlatformArea().getAuthZone(), handle);

        if (!exists(user) || !user.isVisible()) {
            addInvalidFieldError(HANDLE_PARAM, "Member Handle");
        }
    }

    @Override
    @MethodDetails(httpMethodType = HttpMethodType.POST)
    public String execute() throws Exception {
        getNetworkContext().doAreaTask(Area.dao().getNarrativePlatformArea(), new AddUsersToAreaCircle(getAreaCircle(), Collections.singleton(user)));

        setConfirmationMessageForSubsequentRequest(new ConfirmationMessage("Member Added", true));
        return NetworkResponses.redirectResponse();
    }

    public void setHandle(String handle) {
        this.handle = handle;
    }
}
