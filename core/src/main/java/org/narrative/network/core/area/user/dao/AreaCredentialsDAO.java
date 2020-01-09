package org.narrative.network.core.area.user.dao;

import org.narrative.common.persistence.OID;
import org.narrative.network.core.area.user.AreaCredentials;
import org.narrative.network.shared.daobase.GlobalDAOImpl;

/**
 * Date: Sep 27, 2010
 * Time: 9:53:35 AM
 *
 * @author brian
 */
public class AreaCredentialsDAO extends GlobalDAOImpl<AreaCredentials, OID> {
    public AreaCredentialsDAO() {
        super(AreaCredentials.class);
    }

}
