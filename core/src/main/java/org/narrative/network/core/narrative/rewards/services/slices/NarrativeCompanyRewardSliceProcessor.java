package org.narrative.network.core.narrative.rewards.services.slices;

import org.narrative.network.core.narrative.rewards.NarrativeCompanyReward;
import org.narrative.network.core.narrative.rewards.RewardPeriod;
import org.narrative.network.core.narrative.rewards.RewardSlice;
import org.narrative.network.core.narrative.wallet.WalletTransaction;
import org.narrative.network.customizations.narrative.NrveValue;
import org.narrative.network.shared.util.NetworkLogger;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Date: 2019-05-30
 * Time: 07:17
 *
 * @author brian
 */
public class NarrativeCompanyRewardSliceProcessor extends RewardSliceProcessorBase {
    private static final NetworkLogger logger = new NetworkLogger(NarrativeCompanyRewardSliceProcessor.class);

    public NarrativeCompanyRewardSliceProcessor(RewardPeriod period, NrveValue totalNrve) {
        super(period, RewardSlice.NARRATIVE_COMPANY, totalNrve);
    }

    @Override
    protected NetworkLogger getLogger() {
        return logger;
    }

    @Override
    protected Object doMonitoredTask() {
        NarrativeCompanyReward narrativeCompanyReward = NarrativeCompanyReward.dao().getForPeriod(period);

        // bl: no-op if we already have recorded the reward. allows us to safely re-run
        if(exists(narrativeCompanyReward)) {
            return null;
        }

        // bl: record this in a separate transaction so that we record it, commit, and release the wallet lock
        doRootAreaTask(true, period, new RewardSliceRootAreaTask<Object>(slice) {
            @Override
            protected Object doMonitoredTask() {
                // bl: just a single transaction for the total slice amount from the reward period wallet to null to represent the company reward
                WalletTransaction transaction = createTransaction(null, nrveSlice);

                // bl: only other thing to do is to record the reward record!
                NarrativeCompanyReward narrativeCompanyReward = new NarrativeCompanyReward(period, transaction);
                NarrativeCompanyReward.dao().save(narrativeCompanyReward);
                return null;
            }
        });

        return null;
    }
}
