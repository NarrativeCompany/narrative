package org.narrative.network.customizations.narrative.service.impl.user.follow;

import org.narrative.common.persistence.OID;
import org.narrative.network.core.user.User;
import org.narrative.network.customizations.narrative.controller.UserController;
import org.narrative.network.customizations.narrative.service.api.model.FollowsBase;
import org.narrative.network.customizations.narrative.service.api.model.FollowScrollParamsDTO;
import org.narrative.network.shared.tasktypes.AreaTaskImpl;

import java.util.Collections;
import java.util.List;

/**
 * Date: 2019-03-23
 * Time: 12:52
 *
 * @author jonmark
 */
public abstract class BuildFollowsTaskBase<FI, FIDTO, T extends FollowsBase<FIDTO>> extends AreaTaskImpl<T> {
    private final OID userOid;
    private final FollowScrollParamsDTO params;
    private final int maxResults;

    protected User user;

    public BuildFollowsTaskBase(OID userOid, FollowScrollParamsDTO params, int maxResults) {
        super(false);
        this.userOid = userOid;
        this.params = params;
        this.maxResults = maxResults;
    }

    protected void doInitialSetup(User user) {
        // jw: do nothing by default here. Override for cases where initialization data is required.
    }

    // jw: called when running list for a different user, since users can hide lists.
    protected abstract boolean isListHidden(User user);
    // jw: called to get the actual data for the list.
    protected abstract List<FI> getFollowedItems(User user, FollowScrollParamsDTO params, int maxResults);
    // jw: called to add anything into the objects needed to render for the viewer (like follow details)
    protected abstract void setupFollowedItemsForLoggedInUser(User loggedInUser, List<FI> items);
    // jw: map the followed items into DTO objects for the items.
    protected abstract List<FIDTO> getFollowedItemDtos(List<FI> items);
    // jw: necessary to get the scroll parameters from the last item in the list if we should setup for scrolling.
    protected abstract FollowScrollParamsDTO getScrollParamsFromLastItem(FI lastItem);

    // jw: this builds the entries DTO, and could be used for actual results or for empty data for short outs.
    protected abstract T build(List<FIDTO> items, boolean hasMoreItems, FollowScrollParamsDTO scrollParams);

    @Override
    protected T doMonitoredTask() {
        user = User.dao().getForApiParam(userOid, UserController.USER_OID_PARAM);

        // jw: some lists need to setup their result with base data (followerCount for UserFollowerEntriesDTO, for example)
        doInitialSetup(user);

        if (!user.isCurrentUserThisUser()) {
            // jw: If the user is hidding this list from
            if (isListHidden(user)) {
                return generateEmptyResults();
            }
        }

        // fetch the relevant results
        List<FI> followedItems = getFollowedItems(user, params, maxResults);

        // jw: if we don't have any results, that is a empty response.
        if (followedItems.isEmpty()) {
            return generateEmptyResults();
        }

        // jw: if this is a logged in user, let's setup anything on these items necessary for mapping
        if (getNetworkContext().isLoggedInUser()) {
            setupFollowedItemsForLoggedInUser(getNetworkContext().getUser(), followedItems);
        }

        // jw: let's create DTO objects from the followedItems
        List<FIDTO> followedItemDtos = getFollowedItemDtos(followedItems);

        // jw: if we hit our limit of results then we should saturate params for the next request.
        FollowScrollParamsDTO scrollParams = null;
        if (followedItems.size() >= maxResults) {
            FI lastFollowedItem = followedItems.get(followedItems.size()-1);

            scrollParams = getScrollParamsFromLastItem(lastFollowedItem);
        }

        return build(followedItemDtos, scrollParams!=null, scrollParams);
    }

    private T generateEmptyResults() {
        return build(Collections.emptyList(), false, null);
    }
}
