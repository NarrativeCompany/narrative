package org.narrative.network.core.versioning.services;

import org.narrative.network.core.cluster.partition.Partition;
import org.narrative.network.core.cluster.partition.PartitionType;
import org.narrative.network.core.versioning.impl.StandardPatchImpl;

import java.util.Properties;

/**
 * Date: 8/17/12
 * Time: 3:26 PM
 * User: jonmark
 */
public class CreateGlobalFederatedTablePatch extends StandardPatchImpl {
    private final String sqlName;

    public CreateGlobalFederatedTablePatch(String sqlName, PartitionType partitionType) {
        super(sqlName, partitionType);
        assert !partitionType.isGlobal() : "Should never be trying to create a federated table to Global from Global!";
        this.sqlName = sqlName;
    }

    @Override
    public void applyPatch(Partition partition, Properties data) {
        partition.createFederatedTable(sqlName, PartitionType.GLOBAL.getSingletonPartition());
    }
}
