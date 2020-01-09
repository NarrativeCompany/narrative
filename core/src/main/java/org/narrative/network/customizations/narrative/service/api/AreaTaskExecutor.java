package org.narrative.network.customizations.narrative.service.api;

import org.narrative.network.core.area.base.Area;
import org.narrative.network.core.cluster.partition.Partition;
import org.narrative.network.shared.tasktypes.AreaTaskImpl;
import org.narrative.network.shared.tasktypes.CompositionTaskImpl;
import org.narrative.network.shared.tasktypes.GlobalTaskImpl;

/**
 * Injectable Spring wrapper for executing area tasks.
 */
public interface AreaTaskExecutor {

    /**
     * @return {@link Area}
     */
    Area getArea();

    /**
     * Execute a global task
     *
     * @param globalTask The global task to execute.
     * @return The return value from the task.
     */
    <T> T executeGlobalTask(GlobalTaskImpl<T> globalTask);

    /**
     * Execute an area task
     *
     * @param areaTask The area task to execute.
     * @return The return value from the task.
     */
    <T> T executeAreaTask(AreaTaskImpl<T> areaTask);

    /**
     * Execute a composition task
     *
     * @param compositionTask The area task to execute.
     * @return The return value from the task.
     */
    <T> T executeCompositionTask(Partition partition, CompositionTaskImpl<T> compositionTask);

    /**
     * Execute a Narrative platform AreaTask
     *
     * @param areaTask The area task to execute.
     * @return The return value from the task.
     */
    <T> T executeNarrativePlatformAreaTask(AreaTaskImpl<T> areaTask);
}
