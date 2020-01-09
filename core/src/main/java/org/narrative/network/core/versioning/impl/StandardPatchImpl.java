package org.narrative.network.core.versioning.impl;

import org.narrative.network.core.cluster.partition.PartitionType;
import org.narrative.network.core.versioning.StandardPatch;

/**
 * Date: Sep 15, 2006
 * Time: 8:13:13 AM
 *
 * @author Brian
 */
public abstract class StandardPatchImpl extends PatchImpl implements StandardPatch {
    protected StandardPatchImpl(String name, PartitionType partitionType) {
        super(name, partitionType);
    }

    protected StandardPatchImpl(int iteration, PartitionType partitionType) {
        super(iteration, partitionType);
    }

    protected StandardPatchImpl(PartitionType partitionType) {
        super(partitionType);
    }
}
