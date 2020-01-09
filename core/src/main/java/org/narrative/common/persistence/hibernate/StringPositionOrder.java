package org.narrative.common.persistence.hibernate;

import org.narrative.common.persistence.PersistenceUtil;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.criterion.CriteriaQuery;
import org.hibernate.criterion.Order;

/**
 * Created by IntelliJ IDEA.
 * User: jonmark
 * Date: 1/6/17
 * Time: 11:27 AM
 * <p>
 * jw: we want to make sure that we
 */
public class StringPositionOrder extends Order {
    private final boolean ascending;
    private final String propertyName;
    private final String value;

    private StringPositionOrder(String propertyName, String value, boolean ascending) {
        super(propertyName, ascending);
        this.propertyName = propertyName;
        this.value = value;
        this.ascending = ascending;
    }

    @Override
    public String toSqlString(Criteria criteria, CriteriaQuery criteriaQuery) throws HibernateException {
        String[] columns = criteriaQuery.getColumnsUsingProjection(criteria, propertyName);
        StringBuilder fragment = new StringBuilder();
        for (int i = 0; i < columns.length; i++) {
            String inStr = getLocationSql(columns[i], value);
            // jw: if the value is found in the string then lets order by the location of the string, and if its not ensure that the result falls at the end (we want it to be closest to left, with no mathc at the fare right).
            fragment.append("if(" + inStr + " > 0, " + inStr + ", " + Integer.MAX_VALUE + ")");
            fragment.append(ascending ? " asc" : " desc");
            if (i < columns.length - 1) {
                fragment.append(", ");
            }
        }
        return fragment.toString();
    }

    public static Order asc(String propertyName, String value) {
        return new StringPositionOrder(propertyName, value, true);
    }

    public static Order desc(String propertyName, String value) {
        return new StringPositionOrder(propertyName, value, false);
    }

    public static Order order(String propertyName, String value, boolean asc) {
        if (asc) {
            return asc(propertyName, value);
        }

        return desc(propertyName, value);
    }

    public static String getLocationSql(String column, String value) {
        return "instr(" + column + ", '" + PersistenceUtil.MySQLUtils.getStringAfterEscapingWildcardCharsForSqlLikePattern(value, true) + "')";
    }
}
