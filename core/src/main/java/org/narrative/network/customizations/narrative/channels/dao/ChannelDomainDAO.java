package org.narrative.network.customizations.narrative.channels.dao;

import org.narrative.common.persistence.*;
import org.narrative.network.customizations.narrative.channels.Channel;
import org.narrative.network.customizations.narrative.channels.ChannelDomain;
import org.narrative.network.shared.daobase.GlobalDAOImpl;

/**
 * Date: 2019-07-31
 * Time: 07:39
 *
 * @author jonmark
 */
public class ChannelDomainDAO extends GlobalDAOImpl<ChannelDomain, OID> {
    public ChannelDomainDAO() {
        super(ChannelDomain.class);
    }

    public void deleteForChannel(Channel channel) {
        deleteAllByPropertyValue(ChannelDomain.Fields.channel, channel);
    }
}
