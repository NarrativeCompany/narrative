package org.narrative.network.core.narrative.rewards.services.slices;

import org.narrative.network.core.narrative.rewards.RewardSlice;

/**
 * This task exists primarily to inject the RewardPeriod into each root task that we use to process rewards
 * so that each implementation doesn't have to worry about reloading the RewardPeriod into the current session.
 * When using anonymous implementations of this class within a RewardSliceProcessorBase, this `period` variable
 * will hide the RewardSliceProcessorBase.period, which is exactly what we're trying to accomplish here.
 *
 * Date: 2019-05-30
 * Time: 10:10
 *
 * @author brian
 */
public abstract class RewardSliceChunkProcessor extends RewardSliceRootAreaTask<Integer> {
    protected final int chunkSize;

    RewardSliceChunkProcessor(RewardSlice slice, int chunkSize) {
        super(slice);
        this.chunkSize = chunkSize;
    }

    public int getChunkSize() {
        return chunkSize;
    }
}
