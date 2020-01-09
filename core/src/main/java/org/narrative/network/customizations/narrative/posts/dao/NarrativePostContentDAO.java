package org.narrative.network.customizations.narrative.posts.dao;

import org.narrative.common.persistence.OID;
import org.narrative.network.customizations.narrative.posts.NarrativePostContent;
import org.narrative.network.shared.daobase.CompositionDAOImpl;

/**
 * Date: 2019-01-03
 * Time: 11:52
 *
 * @author jonmark
 */
public class NarrativePostContentDAO extends CompositionDAOImpl<NarrativePostContent, OID> {
    public NarrativePostContentDAO() {
        super(NarrativePostContent.class);
    }
}
