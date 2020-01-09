package org.narrative.network.core.cluster.partition.dao;

import org.narrative.common.persistence.OID;
import org.narrative.common.persistence.ObjectPair;
import org.narrative.network.core.cluster.partition.Partition;
import org.narrative.network.core.cluster.partition.PartitionType;
import org.narrative.network.shared.daobase.GlobalDAOImpl;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: paul
 * Date: Nov 28, 2005
 * Time: 11:58:25 PM
 */
public class PartitionDAO extends GlobalDAOImpl<Partition, OID> {

    public PartitionDAO() {
        super(Partition.class);
    }

    public Set<Partition> getAllForType(PartitionType partitionType) {
        return getAllByTypeMap().get(partitionType);
    }

    /**
     * Returns all the partitions grouped by partition type
     *
     * @return a map of partition type to set of partition objects
     */
    public Map<PartitionType, Set<Partition>> getAllByTypeMap() {
        Map<PartitionType, Set<Partition>> allByType = new HashMap<>();
        for (PartitionType type : PartitionType.values()) {
            allByType.put(type, new LinkedHashSet<>());
        }

        // bl: updated this list so that it's sorted by database name. will make looking through lists of partitions
        // a lot nicer since they'll be sorted alphabetically :)
        for (Partition part : getAllOrderBy(Collections.singleton(new ObjectPair<>(Partition.FIELD__DATABASE_NAME__NAME, true)), null, null, true)) {
            // bl: some clusters may be set up to work with both music and eve, in which case they will have
            // partition instances in the db for non-supported partition types (e.g. eve instances with
            // a metadata partition record).  let's support this, but we just have to do an extra check
            // here to make sure the partition type is actually supported.
            allByType.get(part.getPartitionType()).add(part);
        }

        return allByType;
    }

}

