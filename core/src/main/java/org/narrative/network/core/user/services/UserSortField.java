package org.narrative.network.core.user.services;

import org.narrative.common.persistence.hibernate.criteria.CriteriaSort;

/**
 * Date: 6/10/13
 * Time: 3:45 PM
 *
 * @author brian
 */
public interface UserSortField extends CriteriaSort {
    public boolean isSortDescByDefault();
}
