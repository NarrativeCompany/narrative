package org.narrative.network.core.versioning.impl;

import org.narrative.network.core.cluster.partition.PartitionType;
import org.narrative.network.core.versioning.RunForNewVersionPatch;

/**
 * Date: 10/19/17
 * Time: 3:56 PM
 *
 * @author brian
 */
public abstract class NewVersionStandardPatchImpl extends StandardPatchImpl implements RunForNewVersionPatch {
    public NewVersionStandardPatchImpl(String name, PartitionType partitionType) {
        super(name, partitionType);
    }

    public NewVersionStandardPatchImpl(int iteration, PartitionType partitionType) {
        super(iteration, partitionType);
    }

    public NewVersionStandardPatchImpl(PartitionType partitionType) {
        super(partitionType);
    }
}
