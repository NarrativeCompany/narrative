package org.narrative.network.core.user.services;

import org.narrative.network.core.user.User;

/**
 * Date: 2019-05-16
 * Time: 11:25
 *
 * @author jonmark
 */
public interface UserRef {
    String FIELD__USER__NAME = "user";
    String FIELD__USER__COLUMN = FIELD__USER__NAME+"_"+ User.FIELD__OID__NAME;


    User getUser();
    void setUser(User user);
}
