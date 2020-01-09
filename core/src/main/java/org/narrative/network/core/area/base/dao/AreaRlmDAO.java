package org.narrative.network.core.area.base.dao;

import org.narrative.common.persistence.OID;
import org.narrative.network.core.area.base.AreaRlm;
import org.narrative.network.shared.daobase.GlobalDAOImpl;

/**
 * Created by IntelliJ IDEA.
 * User: barry
 * Date: Dec 2, 2005
 * Time: 4:27:54 PM
 * To change this template use File | Settings | File Templates.
 */
public class AreaRlmDAO extends GlobalDAOImpl<AreaRlm, OID> {
    public AreaRlmDAO() {
        super(AreaRlm.class);
    }
}
