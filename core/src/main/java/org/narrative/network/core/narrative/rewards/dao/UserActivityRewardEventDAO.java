package org.narrative.network.core.narrative.rewards.dao;

import org.narrative.common.persistence.*;
import org.narrative.network.core.narrative.rewards.UserActivityRewardEvent;
import org.narrative.network.shared.daobase.GlobalDAOImpl;

/**
 * Date: 2019-05-29
 * Time: 08:35
 *
 * @author brian
 */
public class UserActivityRewardEventDAO extends GlobalDAOImpl<UserActivityRewardEvent, OID> {
    public UserActivityRewardEventDAO() {
        super(UserActivityRewardEvent.class);
    }
}
