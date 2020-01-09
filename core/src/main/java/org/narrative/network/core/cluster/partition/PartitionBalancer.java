package org.narrative.network.core.cluster.partition;

import org.narrative.common.persistence.OID;
import org.narrative.common.persistence.hibernate.EventListenerImpl;
import org.hibernate.HibernateException;
import org.hibernate.event.spi.EvictEvent;
import org.hibernate.event.spi.PostDeleteEvent;
import org.hibernate.event.spi.PostInsertEvent;
import org.hibernate.event.spi.PostUpdateEvent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

/**
 * Created by IntelliJ IDEA.
 * User: paul
 * Date: Dec 4, 2005
 * Time: 1:57:39 PM
 * This class handles handing out the next partition to balance, based on the relative weights of the
 * partitions
 */
public class PartitionBalancer {
    private List<OID> partitionOIDs = new ArrayList<OID>();
    private Random rand = new Random();

    public PartitionBalancer(PartitionType partitionType) {
        Collection<Partition> partitions = Partition.dao().getAllForType(partitionType);
        for (Partition partition : partitions) {
            for (int i = 0; i < partition.getWeight(); i++) {
                partitionOIDs.add(partition.getOid());
            }
        }
    }

    public OID getNextPartitionOID() {

        //return the oid.  Only returning the oid to reduce the amount of sync time
        return partitionOIDs.get(rand.nextInt(partitionOIDs.size()));

    }

    /**
     * This class listens to partition events and resets the partition balancer.
     */
    public static class PartitionListener extends EventListenerImpl {
        public void onPostInsert(PostInsertEvent event) {
            rebalancePartitions(event.getEntity());
        }

        public void onPostUpdate(PostUpdateEvent event) {
            rebalancePartitions(event.getEntity());
        }

        public void onPostDelete(PostDeleteEvent event) {
            rebalancePartitions(event.getEntity());
        }

        public void onEvict(EvictEvent event) throws HibernateException {
            rebalancePartitions(event.getObject());
        }

        private void rebalancePartitions(Object part) {
            assert part instanceof Partition : "Object returned from event was not an instance of Partition.  Coding error!";
            ((Partition) part).getPartitionType().clearBalanceInfo();
        }
    }
}
