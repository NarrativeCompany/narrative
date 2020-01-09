package org.narrative.common.persistence;

import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 * User: paul
 * Date: Nov 24, 2005
 * Time: 1:55:11 AM
 * To change this template use File | Settings | File Templates.
 */
public interface DAOObject<T extends DAO> extends Serializable {
    public static final String FIELD__OID__NAME = "oid";
    public static final String FIELD__OID__COLUMN = FIELD__OID__NAME;

    public OID getOid();

    public void setOid(OID oid);

}
