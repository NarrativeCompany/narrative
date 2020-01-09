package org.narrative.network.customizations.narrative.service.mapper;

import org.narrative.network.customizations.narrative.channels.Channel;
import org.narrative.network.customizations.narrative.channels.DeletedChannel;
import org.narrative.network.customizations.narrative.service.api.model.CurrentUserFollowedItemDTO;
import org.narrative.network.customizations.narrative.service.api.model.DeletedChannelDTO;
import org.narrative.network.customizations.narrative.service.mapper.util.ServiceMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = ServiceMapperConfig.class, uses = {UserMapper.class})
public interface ChannelMapper {
    /**
     * Map from {@link Channel} entity to {@link CurrentUserFollowedItemDTO}.
     *
     * @param channelEntity The Channel entity to map
     * @return The mapped {@link CurrentUserFollowedItemDTO}
     */
    @Mapping(source = "followedByCurrentUser", target = "followed")
    CurrentUserFollowedItemDTO mapChannelEntityToCurrentUserFollowedItem(Channel channelEntity);

    DeletedChannelDTO mapDeletedChannelToDto(DeletedChannel channel);
}
