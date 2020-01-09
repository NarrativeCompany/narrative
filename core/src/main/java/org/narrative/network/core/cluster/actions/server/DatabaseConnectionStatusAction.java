package org.narrative.network.core.cluster.actions.server;

import org.narrative.network.core.cluster.partition.Partition;
import org.narrative.network.core.cluster.partition.PartitionType;

import java.util.Map;
import java.util.Set;

/**
 * Date: Jan 29, 2008
 * Time: 12:03:26 PM
 *
 * @author brian
 */
public class DatabaseConnectionStatusAction extends SystemMonitoringAction {
    private Map<PartitionType, Set<Partition>> partitionsMap;

    public String input() throws Exception {
        partitionsMap = Partition.dao().getAllByTypeMap();
        return INPUT;
    }

    public Map<PartitionType, Set<Partition>> getPartitionsMap() {
        return partitionsMap;
    }
}
