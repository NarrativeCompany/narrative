package org.narrative.network.core.narrative.rewards;

import org.narrative.common.util.UnexpectedError;
import org.narrative.common.util.enums.IntegerEnum;
import org.narrative.network.core.narrative.rewards.services.steps.IssueRewardsStepProcessor;
import org.narrative.network.core.narrative.rewards.services.steps.PopulateContentActivityStepProcessor;
import org.narrative.network.core.narrative.rewards.services.steps.PopulateUserActivityStepProcessor;
import org.narrative.network.core.narrative.rewards.services.steps.ProcessProratedRevenueRefundsStepProcessor;
import org.narrative.network.core.narrative.rewards.services.steps.RewardCleanupStepProcessor;
import org.narrative.network.core.narrative.rewards.services.steps.RewardPeriodStepProcessorBase;
import org.narrative.network.core.narrative.rewards.services.steps.TransferMintedTokensStepProcessor;
import org.narrative.network.core.narrative.rewards.services.steps.TransferProratedRevenueStepProcessor;

/**
 * Date: 2019-05-15
 * Time: 19:19
 *
 * @author jonmark
 */
public enum RewardPeriodStep implements IntegerEnum {
    // jw: this should allow the scheduler to know whether or not to reschedule the processor.
    SCHEDULE_PROCESSING_JOB(0) {
        @Override
        public RewardPeriodStepProcessorBase getProcessor(RewardPeriod period) {
            throw UnexpectedError.getRuntimeException("This step should never be run traditionally. This step should be flagged as completed by ProcessRewardPeriodJob as part of scheduling, well before we actually begin processing.");
        }
    }

    // bl: process refunds and adjust any ProratedMonthRevenue values
    ,PROCESS_PRORATED_REVENUE_REFUNDS(1) {
        @Override
        public ProcessProratedRevenueRefundsStepProcessor getProcessor(RewardPeriod period) {
            return new ProcessProratedRevenueRefundsStepProcessor(period);
        }
    }

    // jw: transfer funds into Monthly rewards.
    ,TRANSFER_MINTED_TOKENS(2) {
        @Override
        public TransferMintedTokensStepProcessor getProcessor(RewardPeriod period) {
            return new TransferMintedTokensStepProcessor(period);
        }
    }
    ,TRANSFER_PRORATED_REVENUE(3) {
        @Override
        public TransferProratedRevenueStepProcessor getProcessor(RewardPeriod period) {
            return new TransferProratedRevenueStepProcessor(period);
        }
    }

    // jw: Populate the major pieces of data needed to drive rewards.
    ,POPULATE_CONTENT_ACTIVITY(4) {
        @Override
        public PopulateContentActivityStepProcessor getProcessor(RewardPeriod period) {
            return new PopulateContentActivityStepProcessor(period);
        }
    }
    ,POPULATE_USER_ACTIVITY(5) {
        @Override
        public PopulateUserActivityStepProcessor getProcessor(RewardPeriod period) {
            return new PopulateUserActivityStepProcessor(period);
        }
    }

    // jw: issue rewards to all members
    ,ISSUE_REWARDS(6) {
        @Override
        public IssueRewardsStepProcessor getProcessor(RewardPeriod period) {
            return new IssueRewardsStepProcessor(period);
        }
    }

    // jw: cleanup processes like transferring remaining funds to the next month.
    ,CLEANUP(7) {
        @Override
        public RewardCleanupStepProcessor getProcessor(RewardPeriod period) {
            return new RewardCleanupStepProcessor(period);
        }
    }
    ;

    public static final String TYPE = "org.narrative.network.core.narrative.rewards.RewardPeriodStep";

    private final int id;

    RewardPeriodStep(int id) {
        this.id = id;
    }

    @Override
    public int getId() {
        return id;
    }

    public abstract RewardPeriodStepProcessorBase getProcessor(RewardPeriod period);
}