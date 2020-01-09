package org.narrative.common.persistence.hibernate;

import org.narrative.common.persistence.PersistenceUtil;

/**
 * Created by IntelliJ IDEA.
 * User: jonmark
 * Date: 6/19/18
 * Time: 12:29 PM
 */
public class StartsWithOrder extends ConditionalOrderBase {
    private final String startsWith;

    public StartsWithOrder(String propertyName, String startsWith, boolean ascending) {
        super(propertyName, ascending);
        this.startsWith = startsWith;
    }

    @Override
    protected String getConditionFragment(String column) {
        return "lower(" + column + ") like '" + PersistenceUtil.MySQLUtils.getStringAfterEscapingWildcardCharsForSqlLikePattern(startsWith, true) + "%'";
    }
}
