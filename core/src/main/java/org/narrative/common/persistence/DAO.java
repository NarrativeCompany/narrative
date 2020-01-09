package org.narrative.common.persistence;

import java.io.Serializable;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: paul
 * Date: Nov 24, 2005
 * Time: 1:53:45 AM
 */
public interface DAO<T extends DAOObject, ID extends Serializable> {

    T get(ID oid);

    List<T> getAllBy(NameValuePair<?>... nameValuePairs);

    T getFirstBy(NameValuePair<?>... nameValuePairs);

}
