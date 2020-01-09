package org.narrative.network.customizations.narrative.service.impl.user.follow;

import org.narrative.common.persistence.OID;
import org.narrative.network.core.area.user.WatchedUser;
import org.narrative.network.core.user.User;
import org.narrative.network.customizations.narrative.service.api.model.FollowScrollParamsDTO;
import org.narrative.network.customizations.narrative.service.api.model.FollowedUserDTO;
import org.narrative.network.customizations.narrative.service.api.model.FollowedUsersDTO;

import java.util.List;

/**
 * Date: 2019-03-23
 * Time: 13:20
 *
 * @author jonmark
 */
public class BuildFollowedUsersTask extends BuildUserFollowsTaskBase<FollowedUsersDTO> {
    public BuildFollowedUsersTask(OID userOid, FollowScrollParamsDTO params, int maxResults) {
        super(userOid, params, maxResults);
    }

    @Override
    protected boolean isListHidden(User user) {
        return user.getPreferences().isHideMyFollows();
    }

    @Override
    protected List<User> getFollowedItems(User user, FollowScrollParamsDTO params, int maxResults) {
        return WatchedUser.dao().getUsersWatchedByUser(user, params, maxResults);
    }

    @Override
    protected void setupFollowedItemsForLoggedInUser(User loggedInUser, List<User> users) {
        // jw: there is no reason to go to the database if this is for the current user. We know they are following
        if (user.isCurrentUserThisUser()) {
            for (User user : users) {
                user.setWatchedByCurrentUser(true);
            }
        } else {
            super.setupFollowedItemsForLoggedInUser(loggedInUser, users);
        }
    }

    @Override
    protected FollowedUsersDTO build(List<FollowedUserDTO> users, boolean hasMoreItems, FollowScrollParamsDTO scrollParams) {
        return FollowedUsersDTO.builder()
                .items(users)
                .hasMoreItems(hasMoreItems)
                .scrollParams(scrollParams)
                .build();
    }
}
