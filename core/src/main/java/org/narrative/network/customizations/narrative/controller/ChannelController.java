package org.narrative.network.customizations.narrative.controller;

import org.narrative.common.persistence.OID;
import org.narrative.network.customizations.narrative.service.api.ChannelService;
import org.narrative.network.customizations.narrative.service.api.model.CurrentUserFollowedItemDTO;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotNull;

/**
 * Date: 2019-02-28
 * Time: 10:12
 *
 * @author brian
 */
@RestController
@RequestMapping("/channels")
@Validated
public class ChannelController {
    public static final String CHANNEL_OID_PARAM = "channelOid";

    private final ChannelService channelService;

    public ChannelController(ChannelService channelService) {
        this.channelService = channelService;
    }

    /**
     * Start following a channel.
     *
     * @param channelOid The channel to start following.
     * @return {@link CurrentUserFollowedItemDTO} representing the updated channel follow.
     */
    @PostMapping("/{" + CHANNEL_OID_PARAM + "}/followers")
    public CurrentUserFollowedItemDTO startFollowingChannel(@NotNull @PathVariable(CHANNEL_OID_PARAM) OID channelOid) {
        return channelService.followChannel(channelOid);
    }

    /**
     * Stop following a channel.
     *
     * @param channelOid The channel to stop following.
     * @return {@link CurrentUserFollowedItemDTO} representing the updated channel follow.
     */
    @DeleteMapping("/{" + CHANNEL_OID_PARAM + "}/followers")
    public CurrentUserFollowedItemDTO stopFollowingChannel(@NotNull @PathVariable(CHANNEL_OID_PARAM) OID channelOid) {
        return channelService.stopFollowingChannel(channelOid);
    }
}
