package org.narrative.network.core.user.dao;

import org.narrative.common.persistence.OID;
import org.narrative.network.core.user.AuthProvider;
import org.narrative.network.core.user.AuthZone;
import org.narrative.network.core.user.UserAuth;
import org.narrative.network.shared.daobase.GlobalDAOImpl;

/**
 * Date: Sep 23, 2010
 * Time: 8:07:54 AM
 *
 * @author brian
 */
public class UserAuthDAO extends GlobalDAOImpl<UserAuth, OID> {
    public UserAuthDAO() {
        super(UserAuth.class);
    }

    public UserAuth getForZoneProviderIdentifier(AuthZone authZone, AuthProvider authProvider, String identifier) {
        return (UserAuth) getGSession().getNamedQuery("userAuth.getForZoneProviderIdentifier").setParameter("authZone", authZone).setParameter("authProvider", authProvider).setParameter("identifier", identifier).uniqueResult();
    }

}
