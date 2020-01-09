package org.narrative.network.shared.tasktypes;

import org.narrative.network.core.cluster.partition.Partition;

/**
 * Created by IntelliJ IDEA.
 * User: paul
 * Date: Sep 26, 2006
 * Time: 11:15:12 AM
 * All tasks which run through PartitionType.doTask() must descend this task.  This allows us to
 * have partition specific logic in tasks common to all tasks that use partitions.
 */
public abstract class AllPartitionsTask<T> extends PartitionTask<T> {

    private Partition currentPartition;

    /**
     * bl: intentionally not providing a default constructor here so that all AllPartitionsTask implementations
     * have to explicitly specify whether they are forceWritable or not.
     *
     * @param forceWritable true if this task must be run in the context of a writable session (i.e. if the task
     *                      performs write operations).
     */
    protected AllPartitionsTask(boolean forceWritable) {
        super(forceWritable);
    }

    /**
     * bl: intentionally not allowing isForceWritable to be overridden.  the forceWritable flag must be supplied
     * in the constructor.
     *
     * @return true if this task must be run in the context of a writable session.
     */
    public final boolean isForceWritable() {
        return super.isForceWritable();
    }

    public Partition getCurrentPartition() {
        return currentPartition;
    }

    public void setCurrentPartition(Partition currentPartition) {
        this.currentPartition = currentPartition;
    }
}
