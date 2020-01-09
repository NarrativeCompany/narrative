package org.narrative.network.core.narrative.rewards.services.steps;

import org.narrative.network.core.narrative.rewards.RewardPeriod;
import org.narrative.network.core.narrative.rewards.RewardPeriodStep;
import org.narrative.network.core.narrative.rewards.services.TransferMintedTokensTask;
import org.narrative.network.shared.util.NetworkLogger;

/**
 * Date: 2019-05-22
 * Time: 14:15
 *
 * @author jonmark
 */
public class TransferMintedTokensStepProcessor extends RewardPeriodStepProcessorBase {
    private static final NetworkLogger logger = new NetworkLogger(TransferMintedTokensStepProcessor.class);

    public TransferMintedTokensStepProcessor(RewardPeriod period) {
        super(period, RewardPeriodStep.TRANSFER_MINTED_TOKENS);
    }

    @Override
    public NetworkLogger getLogger() {
        return logger;
    }

    @Override
    protected Object doMonitoredTask() {
        // jw: this is depressingly easy now that the frameworks and tasks are all in place.
        getAreaContext().doAreaTask(new TransferMintedTokensTask(period));

        return null;
    }
}
