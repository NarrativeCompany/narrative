package org.narrative.network.core.area.user.dao;

import org.narrative.common.persistence.OID;
import org.narrative.network.core.area.user.AreaUserRlm;
import org.narrative.network.core.user.User;
import org.narrative.network.shared.daobase.GlobalDAOImpl;

import java.util.Collection;
import java.util.List;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Created by IntelliJ IDEA.
 * User: paul
 * Date: Dec 16, 2005
 * Time: 3:44:22 PM
 * To change this template use File | Settings | File Templates.
 */
public class AreaUserRlmDAO extends GlobalDAOImpl<AreaUserRlm, OID> {
    public AreaUserRlmDAO() {
        super(AreaUserRlm.class);
    }

    public List<User> getUsersFromAreaUserRlms(Collection<AreaUserRlm> areaUserRlms) {
        List<User> ret = newArrayList(areaUserRlms == null ? 0 : areaUserRlms.size());
        if (areaUserRlms != null) {
            for (AreaUserRlm areaUserRlm : areaUserRlms) {
                ret.add(areaUserRlm.getUser());
            }
        }
        return ret;
    }
}