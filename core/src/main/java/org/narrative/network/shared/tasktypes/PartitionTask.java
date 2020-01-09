package org.narrative.network.shared.tasktypes;

import org.narrative.common.util.Task;
import org.narrative.common.util.ValidationHandler;
import org.narrative.network.core.cluster.partition.Partition;
import org.narrative.network.customizations.narrative.service.api.ValidationContext;

import java.util.function.Consumer;

/**
 * Created by IntelliJ IDEA.
 * User: paul
 * Date: Sep 26, 2006
 * Time: 11:15:12 AM
 * All tasks which run through PartitionType.doTask() must descend this task.  This allows us to
 * have partition specific logic in tasks common to all tasks that use partitions.
 */
public abstract class PartitionTask<T> extends Task<T> {

    private static final PartitionDownErrorReturnValue defaultReturnValue = new PartitionDownErrorReturnValue(false, null);

    protected PartitionTask() {}

    protected PartitionTask(boolean forceWritable) {
        super(forceWritable);
    }

    protected PartitionTask(ValidationHandler validationHandler) {
        super(validationHandler);
    }

    protected PartitionTask(boolean isForceWritable, Consumer<ValidationContext> validationFunction) {
        super(isForceWritable, validationFunction);
    }

    /**
     * If there is a realm down error, this method will be called.  If its overridden by the task and returns true
     * the partition error will not be bubbled up
     *
     * @return
     */
    public PartitionDownErrorReturnValue<T> handlePartitionDownError(Partition partition, Throwable throwable) {
        return defaultReturnValue;
    }
}
