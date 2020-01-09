package org.narrative.network.core.user.dao;

import org.narrative.common.persistence.OID;
import org.narrative.network.core.user.UserKyc;
import org.narrative.network.shared.daobase.GlobalDAOImpl;

public class UserKycDAO extends GlobalDAOImpl<UserKyc, OID> {
    public UserKycDAO() {
        super(UserKyc.class);
    }

    public UserKyc getByUserDetailHashCode(String userDetailHash){
        return (UserKyc) getGSession().getNamedQuery("userKyc.getByUserDetailHash").setParameter("userDetailHash", userDetailHash).uniqueResult();
    }
}
