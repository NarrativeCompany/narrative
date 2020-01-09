package org.narrative.network.customizations.narrative.service.impl.user.follow;

import org.narrative.common.persistence.OID;
import org.narrative.config.StaticConfig;
import org.narrative.network.core.user.User;
import org.narrative.network.customizations.narrative.channels.FollowedChannel;
import org.narrative.network.customizations.narrative.niches.niche.Niche;
import org.narrative.network.customizations.narrative.service.api.model.FollowScrollParamsDTO;
import org.narrative.network.customizations.narrative.service.api.model.FollowedNichesDTO;
import org.narrative.network.customizations.narrative.service.api.model.NicheDTO;
import org.narrative.network.customizations.narrative.service.mapper.NicheDerivativeMapper;

import java.util.List;

/**
 * Date: 2019-03-23
 * Time: 13:35
 *
 * @author jonmark
 */
public class BuildFollowedNichesTask extends BuildFollowsTaskBase<Niche, NicheDTO, FollowedNichesDTO> {
    public BuildFollowedNichesTask(OID userOid, FollowScrollParamsDTO params, int maxResults) {
        super(userOid, params, maxResults);
    }

    @Override
    protected boolean isListHidden(User user) {
        return user.getPreferences().isHideMyFollows();
    }

    @Override
    protected List<Niche> getFollowedItems(User user, FollowScrollParamsDTO params, int maxResults) {
        return Niche.dao().getNichesFollowedByUser(user, params, maxResults);
    }

    @Override
    protected void setupFollowedItemsForLoggedInUser(User loggedInUser, List<Niche> niches) {
        // jw: no need to go to the database for the current user, we can just set all of the niches as followed
        if (user.isCurrentUserThisUser()) {
            for (Niche niche : niches) {
                niche.getChannel().setFollowedByCurrentUser(true);
            }
        } else {
            FollowedChannel.dao().populateChannelConsumersFollowedByCurrentUserField(loggedInUser, niches);
        }
    }

    @Override
    protected List<NicheDTO> getFollowedItemDtos(List<Niche> niches) {
        NicheDerivativeMapper mapper = StaticConfig.getBean(NicheDerivativeMapper.class);
        return mapper.mapNicheEntityListToNicheList(niches);
    }

    @Override
    protected FollowScrollParamsDTO getScrollParamsFromLastItem(Niche lastItem) {
        return FollowScrollParamsDTO.builder()
                .lastItemName(lastItem.getName())
                .lastItemOid(lastItem.getOid())
                .build();
    }

    @Override
    protected FollowedNichesDTO build(List<NicheDTO> niches, boolean hasMoreItems, FollowScrollParamsDTO scrollParams) {
        return FollowedNichesDTO.builder()
                .items(niches)
                .hasMoreItems(hasMoreItems)
                .scrollParams(scrollParams)
                .build();
    }
}
