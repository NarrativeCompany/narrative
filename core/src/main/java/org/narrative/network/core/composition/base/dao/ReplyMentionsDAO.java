package org.narrative.network.core.composition.base.dao;

import org.narrative.common.persistence.OID;
import org.narrative.network.core.composition.base.ReplyMentions;
import org.narrative.network.shared.daobase.CompositionDAOImpl;

/**
 * Created by IntelliJ IDEA.
 * User: jonmark
 * Date: 7/26/16
 * Time: 1:35 PM
 */
public class ReplyMentionsDAO extends CompositionDAOImpl<ReplyMentions, OID> {
    public ReplyMentionsDAO() {
        super(ReplyMentions.class);
    }
}
