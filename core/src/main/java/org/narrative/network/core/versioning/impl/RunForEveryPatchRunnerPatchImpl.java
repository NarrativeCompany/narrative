package org.narrative.network.core.versioning.impl;

import org.narrative.network.core.cluster.partition.PartitionType;
import org.narrative.network.core.versioning.RunForEveryPatchRunnerPatch;

/**
 * Date: 12/19/17
 * Time: 2:07 PM
 *
 * @author brian
 */
public abstract class RunForEveryPatchRunnerPatchImpl extends StandardPatchImpl implements RunForEveryPatchRunnerPatch {
    public RunForEveryPatchRunnerPatchImpl(String name, PartitionType partitionType) {
        super(name, partitionType);
    }
}
