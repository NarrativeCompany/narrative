package org.narrative.network.shared.services;

import org.narrative.network.core.cluster.partition.HighPriorityRunnable;
import org.narrative.network.core.cluster.partition.PartitionGroup;

/**
 * Created by IntelliJ IDEA.
 * User: jonmark
 * Date: 5/9/16
 * Time: 3:26 PM
 */
public interface DelayedResultRunnable extends HighPriorityRunnable {
    static void process(DelayedResultRunnable runnable, Runnable errorRunnable) {
        // we only need to delay the result if this is a writable PartitionGroup. if it's read-only, let's just send the response now.
        if (PartitionGroup.getCurrentPartitionGroup().isReadOnly()) {
            runnable.run();
            return;
        }

        PartitionGroup.addEndOfPartitionGroupRunnable(runnable);

        PartitionGroup.addEndOfPartitionGroupRunnableForError(errorRunnable);
    }
}
