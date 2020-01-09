package org.narrative.network.customizations.narrative.service.impl.common;

import org.narrative.network.core.area.base.Area;
import org.narrative.network.core.cluster.partition.Partition;
import org.narrative.network.customizations.narrative.service.api.AreaTaskExecutor;
import org.narrative.network.shared.tasktypes.AreaTaskImpl;
import org.narrative.network.shared.tasktypes.CompositionTaskImpl;
import org.narrative.network.shared.tasktypes.GlobalTaskImpl;

import static org.narrative.network.shared.util.NetworkCoreUtils.*;

/**
 * Injectable Spring wrapper for executing area tasks.
 */
public class AreaTaskExecutorImpl implements AreaTaskExecutor {

    public AreaTaskExecutorImpl() {}

    public Area getArea() {
        return areaContext().getArea();
    }

    @Override
    public <T> T executeGlobalTask(GlobalTaskImpl<T> globalTask) {
        return areaContext().doGlobalTask(globalTask);
    }

    @Override
    public <T> T executeAreaTask(AreaTaskImpl<T> areaTask) {
        return areaContext().doAreaTask(areaTask);
    }

    @Override
    public <T> T executeCompositionTask(Partition partition, CompositionTaskImpl<T> compositionTask) {
        return networkContext().doCompositionTask(partition, compositionTask);
    }

    @Override
    public <T> T executeNarrativePlatformAreaTask(AreaTaskImpl<T> areaTask) {
        return networkContext().doAreaTask(Area.dao().getNarrativePlatformArea(), areaTask);
    }
}
