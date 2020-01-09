package org.narrative.network.core.narrative.rewards.services.steps;

import org.narrative.common.util.Timer;
import org.narrative.network.core.narrative.rewards.RewardPeriod;
import org.narrative.network.core.narrative.rewards.RewardPeriodStep;
import org.narrative.network.shared.tasktypes.AreaTaskImpl;
import org.narrative.network.shared.util.NetworkLogger;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Date: 2019-05-22
 * Time: 09:37
 *
 * @author jonmark
 */
public abstract class RewardPeriodStepProcessorBase extends AreaTaskImpl<Object> {
    protected final RewardPeriod period;

    private Timer timer;

    RewardPeriodStepProcessorBase(RewardPeriod period, RewardPeriodStep step) {
        assert exists(period) : "We should always have a reward period provided!";
        assert !period.getCompletedSteps().contains(step) : "The Step for this processor should not have already been completed!";

        this.period = period;
    }

    public abstract NetworkLogger getLogger();

    void timeExecution(String log, Runnable r) {
        if(timer==null) {
            timer = new Timer(getLogger(), getProcess());
        }
        timer.start(log);
        try {
            r.run();
        } finally {
            timer.finish();
        }
    }
}
