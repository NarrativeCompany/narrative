package org.narrative.network.shared.daobase;

import org.narrative.common.persistence.DAO;
import org.narrative.common.persistence.DAOObject;
import org.narrative.network.core.cluster.partition.PartitionType;

import java.io.Serializable;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: paul
 * Date: Dec 12, 2005
 * Time: 10:50:28 AM
 * To change this template use File | Settings | File Templates.
 */
public interface NetworkDAO<T extends DAOObject, ID extends Serializable> extends DAO<T, ID> {
    public List<T> getAllForCurrentArea();

    public PartitionType getPartitionType();

    public void save(T obj);

    public void delete(T ojb);
}
