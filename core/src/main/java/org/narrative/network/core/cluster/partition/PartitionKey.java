package org.narrative.network.core.cluster.partition;

import org.narrative.common.persistence.OID;
import org.narrative.common.persistence.hibernate.GSession;

/**
 * PartitionKey is used as the key to the openSessions map in PartitionGroup.  we can't use the actual Partition
 * hibernate objects since those objects can change, even during the course of a single session.  the reason for
 * this is that sessions can be cleared at any point, which means that any subsequent creations of the object
 * will result in a new java object being created representing the same object in the database.
 * <p>
 * PartitionKey solves that problem by implementing equals and hashCode.  the only member properties of the key
 * are the Partition's oid and type.
 * <p>
 * Date: Sep 10, 2007
 * Time: 11:52:05 AM
 *
 * @author brian
 */
class PartitionKey {
    private final OID partitionOid;
    private final PartitionType partitionType;

    PartitionKey(Partition partition) {
        this.partitionOid = partition.getOid();
        this.partitionType = partition.getPartitionType();
    }

    public OID getPartitionOid() {
        return partitionOid;
    }

    public PartitionType getPartitionType() {
        return partitionType;
    }

    public GSession openSession() {
        return getPartitionType().getGSessionFactory().openSession(partitionOid);
    }

    public void closeSession(GSession gSession) {
        // close the session first, which will cause the connection to be reset to non-read-only.
        getPartitionType().getGSessionFactory().closeSession(gSession);
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        PartitionKey that = (PartitionKey) o;

        if (!partitionOid.equals(that.partitionOid)) {
            return false;
        }
        if (partitionType != that.partitionType) {
            return false;
        }

        return true;
    }

    public int hashCode() {
        int result;
        result = partitionOid.hashCode();
        result = 31 * result + partitionType.hashCode();
        return result;
    }
}
