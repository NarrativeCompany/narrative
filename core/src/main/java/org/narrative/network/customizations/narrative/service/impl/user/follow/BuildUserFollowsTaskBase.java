package org.narrative.network.customizations.narrative.service.impl.user.follow;

import org.narrative.common.persistence.OID;
import org.narrative.config.StaticConfig;
import org.narrative.network.core.area.user.WatchedUser;
import org.narrative.network.core.user.User;
import org.narrative.network.customizations.narrative.service.api.model.FollowsBase;
import org.narrative.network.customizations.narrative.service.api.model.FollowScrollParamsDTO;
import org.narrative.network.customizations.narrative.service.api.model.FollowedUserDTO;
import org.narrative.network.customizations.narrative.service.mapper.UserMapper;

import java.util.List;

/**
 * Date: 2019-03-23
 * Time: 13:15
 *
 * @author jonmark
 */
public abstract class BuildUserFollowsTaskBase<T extends FollowsBase<FollowedUserDTO>> extends BuildFollowsTaskBase<User, FollowedUserDTO, T> {
    public BuildUserFollowsTaskBase(OID userOid, FollowScrollParamsDTO params, int maxResults) {
        super(userOid, params, maxResults);
    }

    @Override
    protected void setupFollowedItemsForLoggedInUser(User loggedInUser, List<User> users) {
        // jw: let's select users since we know they are in the cache so this should be performant for Hibernate
        List<User> watchedUsers = WatchedUser.dao().getUsersInListWatchedByUser(getNetworkContext().getUser(), users);

        // jw: regardless if we got records, we need to iterate over every user and set a watched record.
        for (User user : users) {
            user.setWatchedByCurrentUser(watchedUsers.contains(user));
        }
    }

    @Override
    protected List<FollowedUserDTO> getFollowedItemDtos(List<User> users) {
        UserMapper mapper = StaticConfig.getBean(UserMapper.class);
        return mapper.mapUserEntitiesToFollowedUsers(users);
    }

    @Override
    protected FollowScrollParamsDTO getScrollParamsFromLastItem(User lastItem) {
        return FollowScrollParamsDTO.builder()
                .lastItemName(lastItem.getDisplayName())
                .lastItemOid(lastItem.getOid())
                .build();
    }
}
