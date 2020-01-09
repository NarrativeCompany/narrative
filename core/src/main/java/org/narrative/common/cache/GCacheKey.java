package org.narrative.common.cache;

import org.narrative.common.persistence.DAOObject;
import org.narrative.network.core.cluster.partition.PartitionType;

import java.io.Serializable;

public interface GCacheKey{
    //Mainly for OSCache
    String toString();

    boolean equals(Object o);

    int hashCode();

    Serializable getId();

    String getEntityName();

    Serializable getExtraData();

    void setExtraData(Serializable obj);

    boolean doesEntityNameRepresentClass(PartitionType partitionType, Class<? extends DAOObject> objectClass);
}
