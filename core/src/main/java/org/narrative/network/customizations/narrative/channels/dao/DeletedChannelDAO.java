package org.narrative.network.customizations.narrative.channels.dao;

import org.narrative.common.persistence.*;
import org.narrative.network.customizations.narrative.channels.DeletedChannel;
import org.narrative.network.shared.daobase.GlobalDAOImpl;

/**
 * Date: 2019-10-03
 * Time: 12:57
 *
 * @author jonmark
 */
public class DeletedChannelDAO extends GlobalDAOImpl<DeletedChannel, OID> {
    public DeletedChannelDAO() {
        super(DeletedChannel.class);
    }
}
