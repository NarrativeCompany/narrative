package org.narrative.network.core.narrative.rewards.services.slices;

import org.narrative.network.core.narrative.rewards.RewardPeriod;
import org.narrative.network.core.narrative.rewards.RewardSlice;
import org.narrative.network.customizations.narrative.NrveValue;
import org.narrative.network.shared.tasktypes.AreaTaskImpl;
import org.narrative.network.shared.tasktypes.TaskRunner;
import org.narrative.network.shared.util.NetworkLogger;

import java.math.BigDecimal;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Date: 2019-05-22
 * Time: 09:37
 *
 * @author jonmark
 */
public abstract class RewardSliceProcessorBase extends AreaTaskImpl<Object> {
    protected final RewardPeriod period;
    protected final RewardSlice slice;
    final NrveValue totalNrve;
    final NrveValue nrveSlice;

    protected RewardSliceProcessorBase(RewardPeriod period, RewardSlice slice, NrveValue totalNrve) {
        assert exists(period) : "We should always have a RewardPeriod provided!";
        assert slice!=null : "We should always have a RewardSlice the task is for!";
        assert slice.getCurrentPercent().compareTo(BigDecimal.ZERO) > 0 : "Should only create processors for slices that actually have rewards!";

        this.period = period;
        this.slice = slice;
        this.totalNrve = totalNrve;
        this.nrveSlice = slice.getSliceNrve(totalNrve);
    }

    protected abstract NetworkLogger getLogger();

    void chunkProcess(long totalCount, boolean lockRewardPeriod, RewardPeriod rewardPeriodToRefresh, RewardSliceChunkProcessor task) {
        final int chunkSize = task.getChunkSize();
        // bl: default the value to the chunk size for looping purposes
        int countProcessed = chunkSize;
        int totalProcessed = 0;
        while(countProcessed == chunkSize) {
            countProcessed = doRootAreaTask(lockRewardPeriod, rewardPeriodToRefresh, task);
            totalProcessed += countProcessed;
            if(getLogger().isTraceEnabled()) getLogger().trace("Processed " + totalProcessed + " out of " + totalCount);
        }
    }

    /**
     * bl: the session management here is a little funky. always pass in the RewardPeriod to refresh (if any).
     * we can't rely on this.period still being in the proper scope in the event that we have nested RootAreaTasks.
     * the current Session when doRootAreaTask is run isn't necessarily the outermost Session.
     */
    protected <T> T doRootAreaTask(boolean lockRewardPeriod, RewardPeriod rewardPeriodToRefresh, RewardSliceRootAreaTask<T> task) {
        T ret = TaskRunner.doRootAreaTask(getAreaContext().getArea().getOid(), new AreaTaskImpl<T>() {
            @Override
            protected T doMonitoredTask() {
                // bl: load the RewardPeriod into the current session, locked if requested
                RewardPeriod period = lockRewardPeriod ?
                        RewardPeriod.dao().getLocked(RewardSliceProcessorBase.this.period.getOid()) :
                        RewardPeriod.dao().get(RewardSliceProcessorBase.this.period.getOid());
                task.setPeriod(period);
                return getAreaContext().doAreaTask(task);
            }
        });

        // bl: once we are done, assume that the RewardPeriod has been updated (for total distributed),
        // so do a refresh to make sure we're up to date.
        if(rewardPeriodToRefresh!=null) {
            RewardPeriod.dao().refresh(rewardPeriodToRefresh);
        }

        return ret;
    }

    public NrveValue getNrveSlice() {
        return nrveSlice;
    }
}
