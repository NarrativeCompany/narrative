package org.narrative.common.persistence.hibernate;

/**
 * User: brian
 * Date: Aug 30, 2019
 * Time: 12:01:14 AM
 */
public class FeaturedPostOrder extends ConditionalOrderBase {
    private final String sortByPropertyName;
    private FeaturedPostOrder(String futureInstantPropertyName, String sortByPropertyName, boolean ascending) {
        super(futureInstantPropertyName, ascending);
        this.sortByPropertyName = sortByPropertyName;
    }

    @Override
    protected String getConditionFragment(String column) {
        // bl: the HibernateInstantType uses an epoch millisecond value.
        return column + " > (unix_timestamp()*1000)";
    }

    @Override
    protected String getTrueValue() {
        // bl: if the future instant is in the future, then sort by the sortByPropertyName
        return getColumnName(sortByPropertyName);
    }

    @Override
    protected String getFalseValue() {
        // bl: if the future instant is not in the future, then sort by null so that these all come at the end (when sorting descending)
        return "null";
    }

    public static FeaturedPostOrder asc(String futureInstantPropertyName, String sortByPropertyName) {
        return new FeaturedPostOrder(futureInstantPropertyName, sortByPropertyName, true);
    }

    public static FeaturedPostOrder desc(String futureInstantPropertyName, String sortByPropertyName) {
        return new FeaturedPostOrder(futureInstantPropertyName, sortByPropertyName, false);
    }

    public static FeaturedPostOrder sort(String futureInstantPropertyName, String sortByPropertyName, boolean asc) {
        if (asc) {
            return asc(futureInstantPropertyName, sortByPropertyName);
        }

        return desc(futureInstantPropertyName, sortByPropertyName);
    }
}
