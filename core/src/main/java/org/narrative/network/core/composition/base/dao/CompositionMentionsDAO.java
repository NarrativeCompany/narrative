package org.narrative.network.core.composition.base.dao;

import org.narrative.common.persistence.OID;
import org.narrative.network.core.composition.base.CompositionMentions;
import org.narrative.network.shared.daobase.CompositionDAOImpl;

/**
 * Created by IntelliJ IDEA.
 * User: jonmark
 * Date: 7/26/16
 * Time: 1:30 PM
 */
public class CompositionMentionsDAO extends CompositionDAOImpl<CompositionMentions, OID> {
    public CompositionMentionsDAO() {
        super(CompositionMentions.class);
    }
}
