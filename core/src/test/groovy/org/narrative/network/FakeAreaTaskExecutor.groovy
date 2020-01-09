package org.narrative.network

import org.narrative.network.core.area.base.Area
import org.narrative.network.core.cluster.partition.Partition
import org.narrative.network.customizations.narrative.service.api.AreaTaskExecutor
import org.narrative.network.shared.tasktypes.AreaTaskImpl
import org.narrative.network.shared.tasktypes.CompositionTaskImpl
import org.narrative.network.shared.tasktypes.GlobalTaskImpl

class FakeAreaTaskExecutor implements AreaTaskExecutor{
    @Override
    Area getArea() {
        return null
    }

    @Override
    def <T> T executeGlobalTask(GlobalTaskImpl<T> globalTask) {
        globalTask.doMonitoredTask()
    }

    @Override
    def <T> T executeAreaTask(AreaTaskImpl<T> areaTask) {
        areaTask.doMonitoredTask()
    }

    @Override
    def <T> T executeCompositionTask(Partition partition, CompositionTaskImpl<T> compositionTask) {
        compositionTask.doMonitoredTask()
    }

    @Override
    def <T> T executeNarrativePlatformAreaTask(AreaTaskImpl<T> areaTask) {
        areaTask.doMonitoredTask();
    }
}
