package org.narrative.network.core.cluster.partition;

import org.narrative.common.persistence.OID;

import java.io.Serializable;

/**
 * Date: Oct 12, 2006
 * Time: 10:35:31 AM
 *
 * @author Brian
 */
public class PartitionCacheKey implements Serializable {
    private final OID partitionOid;
    private final Serializable wrappedKey;

    public PartitionCacheKey(OID partitionOid, Serializable wrappedKey) {
        this.partitionOid = partitionOid;
        this.wrappedKey = wrappedKey;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        PartitionCacheKey that = (PartitionCacheKey) o;

        if (partitionOid != null ? !partitionOid.equals(that.partitionOid) : that.partitionOid != null) {
            return false;
        }
        if (wrappedKey != null ? !wrappedKey.equals(that.wrappedKey) : that.wrappedKey != null) {
            return false;
        }

        return true;
    }

    public int hashCode() {
        int result;
        result = (partitionOid != null ? partitionOid.hashCode() : 0);
        result = 31 * result + (wrappedKey != null ? wrappedKey.hashCode() : 0);
        return result;
    }

    /**
     * bl: make sure to override toString so that the string cache key version will be used
     * when utilizing memcache.
     *
     * @return the string representation of this cache key
     */
    public String toString() {
        return partitionOid + "-" + wrappedKey.toString();
    }
}
