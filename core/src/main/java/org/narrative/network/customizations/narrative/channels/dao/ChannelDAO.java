package org.narrative.network.customizations.narrative.channels.dao;

import org.narrative.common.persistence.OID;
import org.narrative.network.customizations.narrative.channels.Channel;
import org.narrative.network.shared.daobase.GlobalDAOImpl;

/**
 * Date: 2018-12-19
 * Time: 10:42
 *
 * @author jonmark
 */
public class ChannelDAO extends GlobalDAOImpl<Channel, OID> {
    public ChannelDAO() {
        super(Channel.class);
    }
}
