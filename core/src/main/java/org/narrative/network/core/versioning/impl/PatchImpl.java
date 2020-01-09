package org.narrative.network.core.versioning.impl;

import org.narrative.common.util.IPUtil;
import org.narrative.network.core.cluster.partition.Partition;
import org.narrative.network.core.cluster.partition.PartitionType;
import org.narrative.network.core.versioning.AppliedPatch;
import org.narrative.network.core.versioning.Patch;

/**
 * Created by IntelliJ IDEA.
 * User: paul
 * Date: Mar 20, 2006
 * Time: 2:49:18 PM
 */
public abstract class PatchImpl implements Patch {
    private final PartitionType partitionType;
    private final String name;

    protected PatchImpl(String name, PartitionType partitionType) {
        this.name = name;
        this.partitionType = partitionType;
    }

    protected PatchImpl(int iteration, PartitionType partitionType) {
        this.name = IPUtil.getClassSimpleName(this.getClass()) + Integer.toString(iteration);
        this.partitionType = partitionType;
    }

    protected PatchImpl(PartitionType partitionType) {
        this.name = IPUtil.getClassSimpleName(this.getClass());
        this.partitionType = partitionType;
    }

    public String getName() {
        return name;
    }

    public PartitionType getPartitionType() {
        return partitionType;
    }

    @Override
    public Partition getPartition() {
        return getPartitionType().currentPartition();
    }

    public AppliedPatch getAppliedPatch() {
        return AppliedPatch.dao().getByName(getName(), getPartition());
    }
}
