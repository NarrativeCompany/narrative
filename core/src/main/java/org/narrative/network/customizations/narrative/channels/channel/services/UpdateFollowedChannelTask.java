package org.narrative.network.customizations.narrative.channels.channel.services;

import org.narrative.network.core.user.User;
import org.narrative.network.customizations.narrative.channels.Channel;
import org.narrative.network.customizations.narrative.channels.FollowedChannel;
import org.narrative.network.shared.tasktypes.GlobalTaskImpl;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Date: 9/6/18
 * Time: 7:00 PM
 *
 * @author jonmark
 */
public class UpdateFollowedChannelTask extends GlobalTaskImpl<Channel> {
    private final Channel channel;
    private final boolean followChannel;

    public UpdateFollowedChannelTask(Channel channel, boolean followChannel) {
        this.channel = channel;
        this.followChannel = followChannel;
    }

    @Override
    protected Channel doMonitoredTask() {
        // jw: this method should only ever be called by registered users!
        getNetworkContext().getPrimaryRole().checkRegisteredUser();

        User user = getNetworkContext().getUser();

        // jw: first, let's get the existing follow record, if there is one.
        FollowedChannel follow = FollowedChannel.dao().getFollowedChannel(user, channel);

        // jw: if the followChannel flag already represents the requested state, then there is nothing to do.
        if (followChannel == exists(follow)) {
            channel.setFollowedByCurrentUser(followChannel);
            return channel;
        }

        if (followChannel) {
            follow = new FollowedChannel(user, channel);
            FollowedChannel.dao().save(follow);
            channel.setFollowedByCurrentUser(true);

        } else {
            FollowedChannel.dao().delete(follow);
            channel.setFollowedByCurrentUser(false);
        }

        return channel;
    }
}
