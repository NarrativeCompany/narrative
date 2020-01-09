package org.narrative.common.persistence.hibernate.criteria;

import org.narrative.common.persistence.DAOObject;
import org.narrative.common.persistence.hibernate.HibernateUtil;
import org.hibernate.Criteria;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;

import java.util.Set;

/**
 * Date: 11/30/16
 * Time: 12:08 PM
 *
 * @author brian
 */
public class CriteriaListFieldOrderBy implements CriteriaListOrderBy {
    private final Criteria criteria;
    private final String[] propertyNames;
    private final boolean sortAsc;

    public CriteriaListFieldOrderBy(Criteria criteria, boolean sortAsc, String... propertyNames) {
        this.criteria = criteria;
        this.propertyNames = propertyNames;
        this.sortAsc = sortAsc;
    }

    @Override
    public void addBasicOrder() {
        // bl: we must add the order to the criteria
        criteria.addOrder(HibernateUtil.getOrder(HibernateUtil.makeName(criteria, propertyNames), sortAsc));
    }

    @Override
    public void addToCriteria(Set<Criteria> criteriasInGroupBy, ProjectionList projectionList) {
        addBasicOrder();
        // bl: and if the criteria is new, then add it, as well
        if (!criteriasInGroupBy.contains(criteria)) {
            // bl: all objects should have an "oid" field so always generically group by it.
            projectionList.add(Projections.groupProperty(HibernateUtil.makeName(criteria, DAOObject.FIELD__OID__NAME)));
            criteriasInGroupBy.add(criteria);
        }
    }
}
