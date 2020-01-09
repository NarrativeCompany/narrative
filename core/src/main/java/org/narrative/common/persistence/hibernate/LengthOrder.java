package org.narrative.common.persistence.hibernate;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.criterion.CriteriaQuery;
import org.hibernate.criterion.Order;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Created by IntelliJ IDEA.
 * User: jonmark
 * Date: 9/16/16
 * Time: 2:27 PM
 * <p>
 * Based this on reseearch from the interwebs as well as Barry's IsNullOrder
 */
public class LengthOrder extends Order {
    private final boolean ascending;
    private final String ifContains;
    private final String propertyName;

    private LengthOrder(String propertyName, String ifContains, boolean ascending) {
        super(propertyName, ascending);
        this.propertyName = propertyName;
        this.ifContains = ifContains;
        this.ascending = ascending;
    }

    @Override
    public String toSqlString(Criteria criteria, CriteriaQuery criteriaQuery) throws HibernateException {
        String[] columns = criteriaQuery.getColumnsUsingProjection(criteria, propertyName);
        StringBuilder fragment = new StringBuilder();
        for (int i = 0; i < columns.length; i++) {
            String lengthSql = "length(" + columns[i] + ")";

            if (!isEmpty(ifContains)) {
                // jw: lets see if the column contains this value, if it does use the length of the column, otherwise place this result at the end of all of the other results.
                String inStr = StringPositionOrder.getLocationSql(columns[i], ifContains);

                fragment.append("if(" + inStr + " > 0, " + lengthSql + ", " + Integer.MAX_VALUE + ")");

            } else {
                fragment.append(lengthSql);
            }
            fragment.append(ascending ? " asc" : " desc");
            if (i < columns.length - 1) {
                fragment.append(", ");
            }
        }
        return fragment.toString();
    }

    public static Order asc(String propertyName) {
        return new LengthOrder(propertyName, null, true);
    }

    public static Order asc(String propertyName, String ifContains) {
        return new LengthOrder(propertyName, ifContains, true);
    }

    public static Order desc(String propertyName) {
        return new LengthOrder(propertyName, null, false);
    }

    public static Order desc(String propertyName, String ifContains) {
        return new LengthOrder(propertyName, ifContains, false);
    }

    public static Order order(String propertyName, boolean asc) {
        if (asc) {
            return asc(propertyName);
        }

        return desc(propertyName);
    }

    public static Order order(String propertyName, String ifContains, boolean asc) {
        if (asc) {
            return asc(propertyName, ifContains);
        }

        return desc(propertyName, ifContains);
    }
}
