package org.narrative.network.core.user.services.activityrate;

import org.narrative.network.core.cluster.partition.PartitionType;
import org.narrative.network.core.user.User;
import org.narrative.network.shared.tasktypes.AllPartitionsTask;
import org.narrative.network.shared.tasktypes.TaskOptions;

import java.time.Instant;

/**
 * Date: 2019-06-12
 * Time: 19:10
 *
 * @author jonmark
 */
public abstract class GetUserCompositionActivityRateLimitCountTaskBase extends GetUserActivityRateLimitCountTaskBase {
    protected GetUserCompositionActivityRateLimitCountTaskBase(User user) {
        super(user);
    }

    private int totalActivityCount;

    protected abstract long getUserCompositionActivityCount(User user, Instant after);

    @Override
    protected long getUserActivityRateCount(User user, Instant after) {
        assert totalActivityCount == 0 : "This task should only ever be ran once!";

        // jw: we need to get their activity across all composition partitions.
        PartitionType.COMPOSITION.doTaskInAllPartitionsOfThisType(new TaskOptions(), new AllPartitionsTask<Object>(false) {
            @Override
            protected Object doMonitoredTask() {
                totalActivityCount += getUserCompositionActivityCount(user, after);

                return null;
            }
        });

        return totalActivityCount;
    }
}
