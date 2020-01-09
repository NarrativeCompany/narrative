package org.narrative.network.core.content.base.dao;

import org.narrative.common.persistence.OID;
import org.narrative.network.core.content.base.FutureContent;
import org.narrative.network.shared.daobase.GlobalDAOImpl;

/**
 * Date: Dec 2, 2005
 * Time: 3:23:52 PM
 *
 * @author Brian
 */
public class FutureContentDAO extends GlobalDAOImpl<FutureContent, OID> {
    public FutureContentDAO() {
        super(FutureContent.class);
    }

}
