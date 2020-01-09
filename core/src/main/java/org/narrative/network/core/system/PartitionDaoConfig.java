package org.narrative.network.core.system;

import org.narrative.common.persistence.DAOImpl;
import org.narrative.common.persistence.DAOObject;
import org.narrative.network.core.cluster.partition.PartitionType;

import java.io.Serializable;

/**
 * Date: 3/20/13
 * Time: 9:41 AM
 * User: jonmark
 */
public class PartitionDaoConfig<O extends DAOObject, D extends DAOImpl<O, ? extends Serializable>> {
    private final PartitionType partitionType;
    private final Class<D> daoClass;

    public PartitionDaoConfig(PartitionType partitionType, Class<D> daoClass) {
        this.partitionType = partitionType;
        this.daoClass = daoClass;
    }

    public PartitionType getPartitionType() {
        return partitionType;
    }

    public Class<D> getDaoClass() {
        return daoClass;
    }

}
