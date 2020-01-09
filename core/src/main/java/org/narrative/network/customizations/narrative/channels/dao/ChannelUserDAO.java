package org.narrative.network.customizations.narrative.channels.dao;

import org.narrative.common.persistence.NameValuePair;
import org.narrative.common.persistence.OID;
import org.narrative.common.persistence.ObjectPair;
import org.narrative.common.util.enums.EnumRegistry;
import org.narrative.network.core.user.User;
import org.narrative.network.customizations.narrative.channels.Channel;
import org.narrative.network.customizations.narrative.channels.ChannelConsumer;
import org.narrative.network.customizations.narrative.channels.ChannelRole;
import org.narrative.network.customizations.narrative.channels.ChannelType;
import org.narrative.network.customizations.narrative.channels.ChannelUser;
import org.narrative.network.shared.daobase.GlobalDAOImpl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Date: 2019-07-31
 * Time: 07:57
 *
 * @author jonmark
 */
public class ChannelUserDAO extends GlobalDAOImpl<ChannelUser, OID> {
    public ChannelUserDAO() {
        super(ChannelUser.class);
    }

    public List<ObjectPair<Long, Number>> getUserCountByRoles(Channel channel) {
        return getGSession().createNamedQuery("channelUser.getUserCountByRoles", (Class<ObjectPair<Long, Number>>)(Class)ObjectPair.class)
                .setParameter("channel", channel)
                .list();
    }

    public List<User> getUsersWithRoleInChannel(Channel channel, ChannelRole role) {
        return getGSession().createNamedQuery("channelUser.getUsersWithRoleInChannel", User.class)
                .setParameter("channel", channel)
                .setParameter("role", EnumRegistry.getBitForIntegerEnum(role))
                .list();
    }

    public <T extends Enum<T> & ChannelRole> Map<T,List<User>> getChannelRoleToUsers(Channel channel, Function<ChannelUser, Set<T>> getRolesResolvedFunction) {
        List<ChannelUser> channelUsers = getAllBy(new NameValuePair<>(ChannelUser.Fields.channel, channel));
        Map<T,Set<User>> ret = new HashMap<>();
        for (ChannelUser channelUser : channelUsers) {
            Set<T> roles = getRolesResolvedFunction.apply(channelUser);
            for (T role : roles) {
                Set<User> roleUsers = ret.computeIfAbsent(role, k -> new TreeSet<>(User.DISPLAY_NAME_COMPARATOR));
                roleUsers.add(channelUser.getUser());
            }
        }
        return ret.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> new ArrayList<>(e.getValue())));
    }

    public ChannelUser getForChannelAndUser(Channel channel, User user) {
        return getUniqueBy(
                new NameValuePair<>(ChannelUser.Fields.channel, channel)
                , new NameValuePair<>(ChannelUser.Fields.user, user)
        );
    }

    public List<Channel> getChannelsForUserByTypeAndRole(User user, ChannelType type, ChannelRole role) {
        return getGSession().createNamedQuery("channelUser.getChannelsForUserByTypeAndRole", Channel.class)
                .setParameter("user", user)
                .setParameter("type", type)
                .setParameter("role", EnumRegistry.getBitForIntegerEnum(role))
                .list();
    }

    public List<User> getUsersWithAnyRoleInChannel(Channel channel) {
        return getGSession().createNamedQuery("channelUser.getUsersWithAnyRoleInChannel", User.class)
                .setParameter("channel", channel)
                .list();
    }

    public void deleteForChannel(Channel channel) {
        // jw: pretty easy, just remove all records for the specified channel
        deleteAllByPropertyValue(ChannelUser.Fields.channel, channel);
    }
}
