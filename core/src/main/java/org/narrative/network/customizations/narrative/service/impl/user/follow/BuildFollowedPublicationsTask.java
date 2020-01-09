package org.narrative.network.customizations.narrative.service.impl.user.follow;

import org.narrative.common.persistence.OID;
import org.narrative.config.StaticConfig;
import org.narrative.network.core.user.User;
import org.narrative.network.customizations.narrative.channels.FollowedChannel;
import org.narrative.network.customizations.narrative.publications.Publication;
import org.narrative.network.customizations.narrative.service.api.model.FollowScrollParamsDTO;
import org.narrative.network.customizations.narrative.service.api.model.FollowedPublicationsDTO;
import org.narrative.network.customizations.narrative.service.api.model.PublicationDTO;
import org.narrative.network.customizations.narrative.service.mapper.PublicationMapper;

import java.util.List;

/**
 * Date: 2019-09-25
 * Time: 09:01
 *
 * @author jonmark
 */
public class BuildFollowedPublicationsTask extends BuildFollowsTaskBase<Publication, PublicationDTO, FollowedPublicationsDTO> {
    public BuildFollowedPublicationsTask(OID userOid, FollowScrollParamsDTO params, int maxResults) {
        super(userOid, params, maxResults);
    }

    @Override
    protected boolean isListHidden(User user) {
        return user.getPreferences().isHideMyFollows();
    }

    @Override
    protected List<Publication> getFollowedItems(User user, FollowScrollParamsDTO params, int maxResults) {
        return Publication.dao().getPublicationsFollowedByUser(user, params, maxResults);
    }

    @Override
    protected void setupFollowedItemsForLoggedInUser(User loggedInUser, List<Publication> publications) {
        // jw: no need to go to the database for the current user, we can just set all of the publications as followed
        if (user.isCurrentUserThisUser()) {
            for (Publication publication : publications) {
                publication.getChannel().setFollowedByCurrentUser(true);
            }
        } else {
            FollowedChannel.dao().populateChannelConsumersFollowedByCurrentUserField(loggedInUser, publications);
        }
    }

    @Override
    protected List<PublicationDTO> getFollowedItemDtos(List<Publication> publications) {
        PublicationMapper mapper = StaticConfig.getBean(PublicationMapper.class);
        return mapper.mapPublicationsToDtos(publications);
    }

    @Override
    protected FollowScrollParamsDTO getScrollParamsFromLastItem(Publication lastItem) {
        return FollowScrollParamsDTO.builder()
                .lastItemName(lastItem.getName())
                .lastItemOid(lastItem.getOid())
                .build();
    }

    @Override
    protected FollowedPublicationsDTO build(List<PublicationDTO> publications, boolean hasMoreItems, FollowScrollParamsDTO scrollParams) {
        return FollowedPublicationsDTO.builder()
                .items(publications)
                .hasMoreItems(hasMoreItems)
                .scrollParams(scrollParams)
                .build();
    }
}
