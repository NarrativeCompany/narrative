package org.narrative.common.persistence.hibernate;

/**
 * User: barry
 * Date: Mar 2, 2010
 * Time: 10:36:50 AM
 */
public class IsNullOrder extends ConditionalOrderBase {
    private IsNullOrder(String propertyName, boolean ascending) {
        super(propertyName, ascending);
    }

    @Override
    protected String getConditionFragment(String column) {
        return column + " is null";
    }

    /**
     * Ascending order
     *
     * @param propertyName
     * @return Order
     */
    public static IsNullOrder asc(String propertyName) {
        return new IsNullOrder(propertyName, true);
    }

    /**
     * Descending order
     *
     * @param propertyName
     * @return Order
     */
    public static IsNullOrder desc(String propertyName) {
        return new IsNullOrder(propertyName, false);
    }

    public static IsNullOrder sort(String propertyName, boolean asc) {
        if (asc) {
            return asc(propertyName);
        }

        return desc(propertyName);
    }
}
