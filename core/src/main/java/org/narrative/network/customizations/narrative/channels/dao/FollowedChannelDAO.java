package org.narrative.network.customizations.narrative.channels.dao;

import org.narrative.common.persistence.NameValuePair;
import org.narrative.common.persistence.OID;
import org.narrative.network.core.security.area.community.advanced.AreaCircle;
import org.narrative.network.core.user.User;
import org.narrative.network.customizations.narrative.channels.Channel;
import org.narrative.network.customizations.narrative.channels.ChannelConsumer;
import org.narrative.network.customizations.narrative.channels.FollowedChannel;
import org.narrative.network.shared.daobase.GlobalDAOImpl;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Date: 9/6/18
 * Time: 2:57 PM
 *
 * @author jonmark
 */
public class FollowedChannelDAO extends GlobalDAOImpl<FollowedChannel, OID> {
    public FollowedChannelDAO() {
        super(FollowedChannel.class);
    }

    public List<OID> getUserOidsFollowing(Channel channel) {
        return getGSession().getNamedQuery("followedChannel.getUserOidsFollowing")
                .setParameter("channel", channel)
                .list();
    }

    public List<User> getRandomFollowers(Channel channel, int limit) {
        return getGSession().getNamedQuery("followedChannel.getRandomFollowers")
                .setParameter("channel", channel)
                .setMaxResults(limit)
                .list();
    }

    public int getFollowerCount(Channel channel) {
        return ((Number)getGSession().getNamedQuery("followedChannel.getFollowerCount")
                .setParameter("channel", channel)
                .uniqueResult()).intValue();
    }

    public List<OID> getUserOidsInCircleFollowing(Channel channel, AreaCircle circle) {
        return getGSession().getNamedQuery("followedChannel.getUserOidsInCircleFollowing")
                .setParameter("channel", channel)
                .setParameter("circle", circle)
                .list();
    }

    public FollowedChannel getFollowedChannel(User follower, Channel channel) {
        return getUniqueBy(
                new NameValuePair<>(FollowedChannel.FIELD__FOLLOWER__NAME, follower)
                , new NameValuePair<>(FollowedChannel.FIELD__CHANNEL__NAME, channel)
        );
    }

    private List<OID> getChannelOidsFollowedByUser(User user, Collection<Channel> channels) {
        if (!exists(user) || isEmptyOrNull(channels)) {
            return Collections.emptyList();
        }

        return getGSession().getNamedQuery("followedChannel.getChannelOidsFollowedByUser")
                .setParameter("user", user)
                .setParameterList("channels", channels)
                .list();
    }

    public void populateChannelConsumersFollowedByCurrentUserField(User user, Collection<? extends ChannelConsumer> channelConsumers) {
        populateChannelsFollowedByCurrentUserField(user, channelConsumers.stream().map(ChannelConsumer::getChannel).collect(Collectors.toList()));
    }

    public void populateChannelsFollowedByCurrentUserField(User user, Collection<Channel> channels) {
        // jw: if there are no channels, or we were not given a user, then there is nothing to do.
        if (isEmptyOrNull(channels) || !exists(user)) {
            return;
        }

        // jw: next, let's get a list of all the channel that this user is following from within this set.
        // jw: using a hashset here to improve lookup speed.
        HashSet<OID> followedChannelOids = new HashSet<>(getChannelOidsFollowedByUser(user, channels));

        // jw: finally, because we know we have a user, lets go through all channels and prime the cached value so that
        //     it can be used reliably.
        for (Channel channel : channels) {
            channel.setFollowedByCurrentUser(followedChannelOids.contains(channel.getOid()));
        }
    }

    public void deleteForChannel(Channel channel) {
        deleteAllByPropertyValue(FollowedChannel.FIELD__CHANNEL__NAME, channel);
    }
}
