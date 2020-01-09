package org.narrative.network.core.cluster.actions;

import org.narrative.common.web.HttpMethodType;
import org.narrative.common.web.struts.MethodDetails;
import org.narrative.network.core.area.user.AreaUser;

import java.util.List;

/**
 * Date: 2020-01-03
 * Time: 12:06
 *
 * @author jonmark
 */
public class ManageCircleMembersAction extends ManageCircleMembersActionBase {
    public static final String ACTION_NAME = "circle-members";
    public static final String FULL_ACTION_PATH = "/"+ ACTION_NAME;

    List<AreaUser> circleMembers;

    @Override
    @MethodDetails(httpMethodType = HttpMethodType.GET)
    public String input() throws Exception {
        circleMembers = AreaUser.dao().getAllUsersForAreaCircle(getAreaCircle());

        return INPUT;
    }

    public List<AreaUser> getCircleMembers() {
        return circleMembers;
    }

    @Override
    public Object getSubMenuResource() {
        return ACTION_NAME;
    }

    @Override
    public Object getNestedSubMenuResource() {
        return getCircle();
    }
}
