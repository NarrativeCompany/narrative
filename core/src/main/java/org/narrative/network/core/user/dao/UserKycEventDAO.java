package org.narrative.network.core.user.dao;

import org.narrative.common.persistence.OID;
import org.narrative.network.core.user.UserKycEvent;
import org.narrative.network.shared.daobase.GlobalDAOImpl;

public class UserKycEventDAO extends GlobalDAOImpl<UserKycEvent, OID> {
    public UserKycEventDAO() {
        super(UserKycEvent.class);
    }
}
