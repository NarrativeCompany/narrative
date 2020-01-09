package org.narrative.fakes;

import org.narrative.network.core.cluster.partition.PartitionGroup;
import mockit.Mock;
import mockit.MockUp;

public class FakePartitionGroup extends MockUp<PartitionGroup> {
    @Mock
    public void addEndOfPartitionGroupRunnableForUtilityThread(final Runnable runnable) {
        runnable.run();
    }
}
