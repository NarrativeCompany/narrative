package org.narrative.network.core.moderation.dao;

import org.narrative.common.persistence.*;
import org.narrative.network.core.moderation.ModeratedContent;
import org.narrative.network.shared.daobase.GlobalDAOImpl;

/**
 * Date: 2019-07-31
 * Time: 09:16
 *
 * @author jonmark
 */
public class ModeratedContentDAO extends GlobalDAOImpl<ModeratedContent, OID> {
    public ModeratedContentDAO() {
        super(ModeratedContent.class);
    }
}
