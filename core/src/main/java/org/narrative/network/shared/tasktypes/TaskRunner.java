package org.narrative.network.shared.tasktypes;

import org.narrative.common.persistence.OID;
import org.narrative.common.util.IPUtil;
import org.narrative.network.core.area.base.Area;
import org.narrative.network.core.cluster.partition.Partition;
import org.narrative.network.core.cluster.partition.PartitionGroup;
import org.narrative.network.core.cluster.partition.PartitionType;
import org.narrative.network.shared.context.AreaContext;
import org.narrative.network.shared.context.NetworkContext;
import org.narrative.network.shared.context.NetworkContextImpl;
import org.narrative.network.shared.context.NetworkContextImplBase;

import static org.narrative.network.shared.util.NetworkCoreUtils.*;

/**
 * Created by IntelliJ IDEA.
 * User: paul
 * Date: Dec 19, 2005
 * Time: 1:42:48 PM
 */
public class TaskRunner {

    public static <T> T doRootAreaTask(final OID areaOid, final AreaTaskImpl<T> task) {
        return doRootGlobalTask(new GlobalTaskImpl<T>(task.isForceWritable()) {
            protected T doMonitoredTask() {
                Area area = Area.dao().get(areaOid);
                // bl: register this task so that we flush once the AreaTask has completed while the realm session is still in
                // scope to make sure the flush (and any event listeners) have access to the current realm partition, if necessary.
                PartitionGroup.getCurrentPartitionGroup().registerTaskForFlushingOnSuccess(task);
                return getNetworkContext().doAreaTask(area, task);
            }
        });
    }

    public static <T> T doRootAreaTaskInNewContext(final OID areaOid, final AreaTaskImpl<T> task) {
        return doRootGlobalTaskInNewContext(new GlobalTaskImpl<T>(task.isForceWritable()) {
            protected T doMonitoredTask() {
                Area area = Area.dao().get(areaOid);
                return getNetworkContext().doAreaTask(area, task);
            }
        });
    }

    public static <T> T doAreaTaskAndCreateRootIfNecessary(final OID areaOid, AreaTaskImpl<T> task, TaskIsolationLevel taskIsolationLevel) {
        // need to check if the network context is set since this method will be called sometimes
        // while a network context is set, and sometimes while it is not.
        if (NetworkContextImpl.isNetworkContextSet()) {
            return networkContext().doAreaTask(Area.dao().get(areaOid), new TaskOptions(taskIsolationLevel), task);
        }

        return TaskRunner.doRootAreaTask(areaOid, task);
    }

    public static <T> T doGlobalTaskAndCreateRootIfNecessary(GlobalTaskImpl<T> task, TaskIsolationLevel taskIsolationLevel) {
        // need to check if the network context is set since this method will be called sometimes
        // while a network context is set, and sometimes while it is not.
        if (NetworkContextImpl.isNetworkContextSet()) {
            // do this in a new session
            return networkContext().doGlobalTask(new TaskOptions(taskIsolationLevel), task);
        }

        return TaskRunner.doRootGlobalTask(task);
    }

    public static <T> T doRootGlobalTask(GlobalTaskImpl<T> task) {
        return doRootGlobalTask(task, false);
    }

    public static <T> T doRootGlobalTask(final GlobalTaskImpl<T> task, boolean bypassErrorStatisticRecording) {
        TaskOptions taskOptions = new TaskOptions(TaskIsolationLevel.ISOLATED);
        taskOptions.setBypassErrorStatisticRecording(bypassErrorStatisticRecording);
        try {
            return doGlobalTask(null, taskOptions, task);
        } finally {
            // process any endofthread operations
            IPUtil.onEndOfThread();
        }
    }

    public static <T> T doRootGlobalTaskInNewContext(final GlobalTaskImpl<T> task) {
        boolean wasNetworkContextPreviouslySet = isNetworkContextSet();
        TaskOptions taskOptions = new TaskOptions(TaskIsolationLevel.ISOLATED);
        try {
            return doGlobalTask(new NetworkContextImpl(), taskOptions, task);
        } finally {
            // process any endofthread operations if there was no network context prior
            if (!wasNetworkContextPreviouslySet) {
                IPUtil.onEndOfThread();
            }
        }
    }

    public static <T> T doGlobalTask(NetworkContext contextToUse, GlobalTaskImpl<T> task) {
        return doGlobalTask(contextToUse, new TaskOptions(contextToUse), task);
    }

    public static <T> T doGlobalTask(NetworkContext contextToUse, TaskOptions taskOptions, GlobalTaskImpl<T> task) {
        taskOptions.setNetworkContextToUse(contextToUse);
        return doTask(PartitionType.GLOBAL.getSingletonPartition(), taskOptions, task);
    }

    public static <T> T doRootTaskInDuplicateContext(final NetworkContext networkContext, final GlobalTaskImpl<T> task) {
        return TaskRunner.doRootGlobalTask(new GlobalTaskImpl<T>(task.isForceWritable()) {
            @Override
            protected T doMonitoredTask() {
                NetworkContextImplBase networkContextImplBase = (NetworkContextImplBase) getNetworkContext();
                // bl: set the primary role on the new/current NetworkContext based on the old/original networkContext
                networkContextImplBase.setPrimaryRole(networkContext.getPrimaryRole());
                // if we are duplicating a previous AreaContext, then we need to do an area task
                if (networkContext instanceof AreaContext) {
                    AreaContext areaContext = (AreaContext) networkContext;
                    // bl: since the old context may now be out of scope, we need to create a new Area object
                    // so that it is associated with the currently active session
                    Area area = Area.dao().get(areaContext.getArea().getOid());
                    return getNetworkContext().doAreaTask(area, new AreaTaskImpl<T>() {
                        @Override
                        protected T doMonitoredTask() {
                            return getNetworkContext().doGlobalTask(task);
                        }
                    });
                }
                return getNetworkContext().doGlobalTask(task);
            }
        });
    }

    /**
     * Does a task using the same context passed in
     * the current area and wraps it in a transaction.
     *
     * @param contextToUse AreaContext to use
     * @param task         AreaTaskImpl to execute
     * @return the return value from the executed task
     */
    public static <T> T doAreaTask(AreaContext contextToUse, AreaTaskImpl<T> task) {
        return doAreaTask(contextToUse, new TaskOptions(), task);
    }

    public static <T> T doAreaTask(AreaContext contextToUse, TaskOptions taskOptions, AreaTaskImpl<T> task) {
        taskOptions.setNetworkContextToUse(contextToUse);
        return doTask(PartitionType.GLOBAL.getSingletonPartition(), taskOptions, task);
    }

//    /**
//     * Does an area task with a new context and an empty role.
//     * @param area
//     * @param task
//     * @return
//     */
//    public static <T> T doAreaTask(Area area, NetworkContext ctx, AreaTaskImpl<T> task) {
//
//        AreaContext oldAreaContext = task.getAreaContext();
//        task.setAreaContext(new AreaContextImpl(task.getGlobalContext(), area, areaRole));
//        try {
//            return doMonitoredTask(area.getRealmPartition(), task);
//        } finally {
//            task.setAreaContext(oldAreaContext);
//        }
//    }

    /*public static <T> T doDialogTask(DialogCmp dialog, CompositionTaskImpl<T> task) {
        return doCompositionTask(dialog.getCompositionPartition(), task);
    }

    public static <T> T doContentTask(Content content, CompositionTaskImpl<T> task) {
        Partition part = Partition.dao().get(content.getCompositionPartitionOid());
        return doCompositionTask(part, task);
    }*/

    public static <T> T doCompositionTask(NetworkContext networkContextToUse, Partition compositionPartition, TaskOptions taskOptions, CompositionTaskImpl<T> task) {
        assert PartitionType.COMPOSITION.equals(compositionPartition.getPartitionType()) : "Composition tasks can only be done on COMPOSITION Partitions! pt/" + compositionPartition.getPartitionType();
        taskOptions.setNetworkContextToUse(networkContextToUse);
        task.setPartition(compositionPartition);
        return doTask(compositionPartition, taskOptions, task);
    }

    public static <T> T doTask(Partition partition, TaskOptions taskOptions, PartitionTask<T> task) {
        return partition.getPartitionType().doTask(partition, taskOptions, task);
    }

}
