package org.narrative.network.core.area.user.dao;

import org.narrative.common.persistence.NameValuePair;
import org.narrative.common.persistence.OID;
import org.narrative.network.core.area.base.AreaRlm;
import org.narrative.network.core.area.user.SandboxedAreaUser;
import org.narrative.network.shared.daobase.GlobalDAOImpl;

/**
 * Date: Dec 18, 2009
 * Time: 12:42:26 PM
 *
 * @author Jonmark Weber
 */
public class SandboxedAreaUserDAO extends GlobalDAOImpl<SandboxedAreaUser, OID> {
    public SandboxedAreaUserDAO() {
        super(SandboxedAreaUser.class);
    }

    public SandboxedAreaUser getForAreaRlmAndDisplayName(AreaRlm areaRlm, String displayName) {
        return getFirstBy(new NameValuePair<>(SandboxedAreaUser.FIELD__AREA_RLM__NAME, areaRlm), new NameValuePair<>(SandboxedAreaUser.FIELD__DISPLAY_NAME__NAME, displayName));
    }
}