package org.narrative.network.customizations.narrative.service.impl.user.follow;

import org.narrative.common.persistence.OID;
import org.narrative.network.core.area.user.WatchedUser;
import org.narrative.network.core.user.User;
import org.narrative.network.customizations.narrative.service.api.model.FollowScrollParamsDTO;
import org.narrative.network.customizations.narrative.service.api.model.FollowedUserDTO;
import org.narrative.network.customizations.narrative.service.api.model.UserFollowersDTO;

import java.util.List;

/**
 * Date: 2019-03-23
 * Time: 13:25
 *
 * @author jonmark
 */
public class BuildUserFollowersTask extends BuildUserFollowsTaskBase<UserFollowersDTO> {
    private final UserFollowersDTO.UserFollowersDTOBuilder builder = UserFollowersDTO.builder();

    public BuildUserFollowersTask(OID userOid, FollowScrollParamsDTO params, int maxResults) {
        super(userOid, params, maxResults);
    }

    @Override
    protected void doInitialSetup(User user) {
        builder.totalFollowers(WatchedUser.dao().getTotalWatcherCountForUser(user));
    }

    @Override
    protected boolean isListHidden(User user) {
        return user.getPreferences().isHideMyFollowers();
    }

    @Override
    protected List<User> getFollowedItems(User user, FollowScrollParamsDTO params, int maxResults) {
        return WatchedUser.dao().getUsersWatchingUser(user, params, maxResults);
    }

    @Override
    protected UserFollowersDTO build(List<FollowedUserDTO> items, boolean hasMoreItems, FollowScrollParamsDTO scrollParams) {
        return builder
                .items(items)
                .hasMoreItems(hasMoreItems)
                .scrollParams(scrollParams)
                .build();
    }
}
