package org.narrative.network.customizations.narrative.service.impl.channel;

import org.narrative.common.persistence.OID;
import org.narrative.network.customizations.narrative.channels.Channel;
import org.narrative.network.customizations.narrative.channels.channel.services.UpdateFollowedChannelTask;
import org.narrative.network.customizations.narrative.controller.ChannelController;
import org.narrative.network.customizations.narrative.service.api.AreaTaskExecutor;
import org.narrative.network.customizations.narrative.service.api.ChannelService;
import org.narrative.network.customizations.narrative.service.api.model.CurrentUserFollowedItemDTO;
import org.narrative.network.customizations.narrative.service.mapper.ChannelMapper;
import org.springframework.stereotype.Service;

@Service
public class ChannelServiceImpl implements ChannelService {
    private final AreaTaskExecutor areaTaskExecutor;
    private final ChannelMapper channelMapper;

    public ChannelServiceImpl(AreaTaskExecutor areaTaskExecutor, ChannelMapper channelMapper) {
        this.areaTaskExecutor = areaTaskExecutor;
        this.channelMapper = channelMapper;
    }

    @Override
    public CurrentUserFollowedItemDTO followChannel(OID channelOid) {
        Channel channel = Channel.dao().getForApiParam(channelOid, ChannelController.CHANNEL_OID_PARAM);

        channel = areaTaskExecutor.executeGlobalTask(new UpdateFollowedChannelTask(channel, true));

        return channelMapper.mapChannelEntityToCurrentUserFollowedItem(channel);
    }

    @Override
    public CurrentUserFollowedItemDTO stopFollowingChannel(OID channelOid) {
        Channel channel = Channel.dao().getForApiParam(channelOid, ChannelController.CHANNEL_OID_PARAM);

        channel = areaTaskExecutor.executeGlobalTask(new UpdateFollowedChannelTask(channel, false));

        return channelMapper.mapChannelEntityToCurrentUserFollowedItem(channel);
    }
}
