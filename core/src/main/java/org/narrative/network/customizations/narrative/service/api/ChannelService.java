package org.narrative.network.customizations.narrative.service.api;

import org.narrative.common.persistence.OID;
import org.narrative.network.customizations.narrative.service.api.model.CurrentUserFollowedItemDTO;

/**
 * Channel operations service.
 */
public interface ChannelService {
    /**
     * Ensures that the current user is following the specified Channel.
     *
     * @param channelOid OID The channel the user wants to start following.
     * @return {@link CurrentUserFollowedItemDTO} representing the Channel follow that was updated
     */
    CurrentUserFollowedItemDTO followChannel(OID channelOid);

    /**
     * Ensures that the current user is not following the specified Channel.
     *
     * @param channelOid OID The channel the user wants to stop following.
     * @return {@link CurrentUserFollowedItemDTO} representing the Channel follow that was updated
     */
    CurrentUserFollowedItemDTO stopFollowingChannel(OID channelOid);
}
