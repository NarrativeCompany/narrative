package org.narrative.common.persistence.hibernate;

import org.narrative.network.core.user.User;
import org.hibernate.Criteria;

/**
 * Created by IntelliJ IDEA.
 * User: jonmark
 * Date: 6/19/18
 * Time: 12:47 PM
 */
public class MentionsFieldOrder extends StartsWithOrder {
    private final Criteria userCriteria;

    public MentionsFieldOrder(Criteria userCriteria, String mentionsFilter, boolean ascending) {
        super(HibernateUtil.makeName(userCriteria, User.FIELD__USERNAME__NAME), mentionsFilter, ascending);
        this.userCriteria = userCriteria;
    }

    @Override
    protected String getTrueValue() {
        // jw: if the username starts with the filter, then order by the username since it is the most relevant field
        return getColumnName(HibernateUtil.makeName(userCriteria, User.FIELD__USERNAME__NAME));
    }

    @Override
    protected String getFalseValue() {
        // jw: if the username does not start with the filter, then the match must have been on the displayName
        return getColumnName(HibernateUtil.makeName(userCriteria, User.FIELD__DISPLAY_NAME__NAME));
    }
}
