package org.narrative.common.persistence.hibernate.criteria;

import org.narrative.common.util.UnexpectedError;
import org.hibernate.Criteria;
import org.hibernate.criterion.ProjectionList;

import java.util.Set;

/**
 * Date: 8/30/19
 * Time: 11:28 AM
 *
 * @author brian
 */
public interface CriteriaListOrderBy {
    void addBasicOrder();
    default void addToCriteria(Set<Criteria> criteriasInGroupBy, ProjectionList projectionList) {
        throw UnexpectedError.getRuntimeException("Should only use addToCriteria on order by types that support it!");
    }
}
