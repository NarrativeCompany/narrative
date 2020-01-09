package org.narrative.common.persistence.hibernate;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.criterion.CriteriaQuery;
import org.hibernate.criterion.Order;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Created by IntelliJ IDEA.
 * User: jonmark
 * Date: 6/19/18
 * Time: 12:19 PM
 */
public abstract class ConditionalOrderBase extends Order {
    private final boolean ascending;
    private final String propertyName;

    protected ConditionalOrderBase(String propertyName, boolean ascending) {
        super(propertyName, ascending);
        this.propertyName = propertyName;
        this.ascending = ascending;
    }

    protected abstract String getConditionFragment(String column);

    private Criteria criteria;
    private CriteriaQuery criteriaQuery;

    protected String getColumnName(String propertyName) {
        String[] columns = criteriaQuery.getColumnsUsingProjection(criteria, propertyName);

        assert !isEmptyOrNull(columns) && columns.length == 1 : "Should only ever have a single column when this is used!";

        return columns[0];
    }

    @Override
    public String toSqlString(Criteria criteria, CriteriaQuery criteriaQuery) throws HibernateException {
        this.criteria = criteria;
        this.criteriaQuery = criteriaQuery;

        String columnName = getColumnName(propertyName);
        StringBuilder fragment = new StringBuilder();
        fragment.append("if(");
        fragment.append(getConditionFragment(columnName));
        fragment.append(",");
        fragment.append(getTrueValue());
        fragment.append(",");
        fragment.append(getFalseValue());
        fragment.append(")");
        fragment.append(ascending ? " asc" : " desc");

        return fragment.toString();
    }

    protected String getTrueValue() {
        return "1";
    }

    protected String getFalseValue() {
        return "0";
    }
}
