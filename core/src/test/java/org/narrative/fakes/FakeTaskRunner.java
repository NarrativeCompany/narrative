package org.narrative.fakes;

import org.narrative.common.persistence.OID;
import org.narrative.network.shared.tasktypes.AreaTaskImpl;
import org.narrative.network.shared.tasktypes.TaskRunner;
import mockit.Mock;
import mockit.MockUp;

public class FakeTaskRunner extends MockUp<TaskRunner> {
    @Mock
    public <T> T doRootAreaTask(final OID areaOid, final AreaTaskImpl<T> task) {
        task.doTask();
        return null;
    }
}
