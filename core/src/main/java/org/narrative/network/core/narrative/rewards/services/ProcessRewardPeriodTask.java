package org.narrative.network.core.narrative.rewards.services;

import org.narrative.common.util.Timer;
import org.narrative.common.util.UnexpectedError;
import org.narrative.config.StaticConfig;
import org.narrative.config.cache.spring.CacheManagerDefaultConfig;
import org.narrative.network.core.area.base.Area;
import org.narrative.network.core.cluster.partition.PartitionGroup;
import org.narrative.network.core.narrative.rewards.RewardPeriod;
import org.narrative.network.core.narrative.rewards.RewardPeriodStep;
import org.narrative.network.core.narrative.rewards.services.steps.RewardPeriodStepProcessorBase;
import org.narrative.network.core.system.NetworkRegistry;
import org.narrative.network.shared.tasktypes.AreaTaskImpl;
import org.narrative.network.shared.tasktypes.GlobalTaskImpl;
import org.narrative.network.shared.tasktypes.TaskRunner;
import org.narrative.network.shared.util.NetworkLogger;
import org.narrative.shared.redisson.management.RedissonObjectManager;

import java.time.Instant;
import java.util.EnumSet;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Date: 2019-05-22
 * Time: 09:20
 *
 * @author jonmark
 */
public class ProcessRewardPeriodTask extends AreaTaskImpl<Object> {
    private static final NetworkLogger logger = new NetworkLogger(ProcessRewardPeriodTask.class);

    private final RewardPeriod period;

    public ProcessRewardPeriodTask(RewardPeriod period) {
        this.period = period;
        assert exists(period) : "Should always be provided a period!";
        assert !period.isCompleted() : "The provided period should not be completed!";
    }

    @Override
    protected Object doMonitoredTask() {
        if(!period.isEligibleForRewardProcessing()) {
            throw UnexpectedError.getRuntimeException("Should never process rewards until the period is over! period/" + period.getPeriod());
        }

        Timer timer = new Timer(logger, getProcess());

        // jw: the first thing we need to do is iterate over all RewardPeriodSteps and process the ones not completed already.
        for (RewardPeriodStep step : RewardPeriodStep.values()) {
            if (period.getCompletedSteps().contains(step)) {
                if(logger.isInfoEnabled()) logger.info("Skipping " + step + " as already completed.");
                continue;
            }

            timer.start(period.getPeriod() + ": " + step);
            try {
                // jw: here is where things get tricky. We want to ensure that each step happens in its own transaction, so we need
                //     to setup a new RootAreaTask to run this within.
                TaskRunner.doRootAreaTask(Area.dao().getNarrativePlatformArea().getOid(), new AreaTaskImpl<Object>(true) {
                    @Override
                    protected Object doMonitoredTask() {
                        RewardPeriod period = RewardPeriod.dao().get(ProcessRewardPeriodTask.this.period.getOid());
                        // jw: let's make sure the step still needs to be processed.
                        if (period.getCompletedSteps().contains(step)) {
                            return null;
                        }

                        RewardPeriodStepProcessorBase stepProcessor = step.getProcessor(period);
                        if (stepProcessor==null) {
                            throw UnexpectedError.getRuntimeException("Every step should provide a processor!");
                        }

                        // jw: now we just need to execute the processor and trust it's doing what it needs to.
                        getAreaContext().doAreaTask(stepProcessor);

                        RewardPeriod.dao().refreshForLock(period);

                        // jw: with the processor being done, we can flag this step as complete
                        period.getCompletedSteps().add(step);

                        return null;
                    }
                });

                // jw: before we continue iterating let's reload the period since the above transaction should have made a
                //     change to the underlying data.
                RewardPeriod.dao().refresh(period);

                // jw: let's ensure that the refreshed object has the step marked as completed.
                if (!period.getCompletedSteps().contains(step)) {
                    throw UnexpectedError.getRuntimeException("Now that we have finished processing this step it should be complete! step/" + step);
                }
            } finally {
                timer.finish();
            }
        }

        // jw: now that we have processed all steps let's ensure they are all marked as completed.
        if (!period.getCompletedSteps().containsAll(EnumSet.allOf(RewardPeriodStep.class))) {
            throw UnexpectedError.getRuntimeException("All RewardPeriodSteps should be completed by this point!");
        }

        // jw: now is the simple part, mark the period as complete
        period.setCompletedDatetime(Instant.now());

        // bl: now that the RewardPeriod is complete, schedule the reward notification email job
        // bl: do it via end of PartitionGroup runnable so that we know this transaction has completed
        // bl: don't need to attempt to send emails when we're installing
        if(!NetworkRegistry.getInstance().isInstalling()) {
            PartitionGroup.getCurrentPartitionGroup().addEndOfGroupRunnable(() -> TaskRunner.doRootGlobalTask(new GlobalTaskImpl<Object>() {
                @Override
                protected Object doMonitoredTask() {
                    RewardPeriod rewardPeriod = RewardPeriod.dao().get(period.getOid());
                    SendRewardNotificationEmailsJob.schedule(rewardPeriod);
                    return null;
                }
            }));
        }

        // bl: also, clear the HQ rewards cache so that the new RewardPeriod's stats will show up immediately
        PartitionGroup.getCurrentPartitionGroup().addEndOfGroupRunnable(() -> {
            RedissonObjectManager redissonObjectManager = StaticConfig.getBean(RedissonObjectManager.class);
            redissonObjectManager.clearLocalCache(CacheManagerDefaultConfig.CacheName.CACHE_REWARDSSERVICE_REWARD_PERIOD_STATS);
            redissonObjectManager.clearRedisCache(CacheManagerDefaultConfig.CacheName.CACHE_REWARDSSERVICE_REWARD_PERIOD_STATS);
            redissonObjectManager.clearLocalCache(CacheManagerDefaultConfig.CacheName.CACHE_STATSSERVICE_STATS_OVERVIEW);
            redissonObjectManager.clearRedisCache(CacheManagerDefaultConfig.CacheName.CACHE_STATSSERVICE_STATS_OVERVIEW);
        });

        if(logger.isInfoEnabled()) logger.info("Finish processing reward period " + period.getPeriod());

        return null;
    }
}
