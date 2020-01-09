package org.narrative.network.core.versioning.impl;

import org.narrative.common.persistence.hibernate.DatabaseResources;
import org.narrative.network.core.cluster.partition.PartitionType;
import org.narrative.network.core.versioning.BootstrapPatch;

/**
 * Date: Jun 5, 2006
 * Time: 3:42:30 PM
 *
 * @author Brian
 */
public class BootstrapMultiNamedQueryPatch implements BootstrapPatch {
    private final String baseQueryName;

    public BootstrapMultiNamedQueryPatch(String baseQueryName) {
        this.baseQueryName = baseQueryName;
    }

    public String getName() {
        return baseQueryName;
    }

    public void applyPatch() {
        DatabaseResources resources = PartitionType.GLOBAL.getSingletonPartition().getBootstrapDatabaseResources();
        resources.executeMultiNamedStatements(baseQueryName);
    }
}
