package org.narrative.common.persistence.hibernate;

import org.hibernate.Criteria;

/**
 * Created by IntelliJ IDEA.
 * User: jonmark
 * Date: 6/19/18
 * Time: 12:38 PM
 */
public class MentionsLengthOrder extends MentionsFieldOrder {
    public MentionsLengthOrder(Criteria userCriteria, String mentionsFilter, boolean ascending) {
        super(userCriteria, mentionsFilter, ascending);
    }

    @Override
    protected String getTrueValue() {
        // jw: if the username starts with the filter, then order by the length of the username
        return "length(" + super.getTrueValue() + ")";
    }

    @Override
    protected String getFalseValue() {
        // jw: if the username does not start with the filter, then the match must have been on the displayName
        return "length(" + super.getFalseValue() + ")";
    }
}
