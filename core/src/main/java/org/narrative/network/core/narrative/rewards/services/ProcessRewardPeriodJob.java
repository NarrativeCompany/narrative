package org.narrative.network.core.narrative.rewards.services;

import org.narrative.common.persistence.OID;
import org.narrative.common.persistence.OIDGenerator;
import org.narrative.common.util.Debug;
import org.narrative.common.util.IPDateUtil;
import org.narrative.network.core.area.base.Area;
import org.narrative.network.core.cluster.partition.PartitionGroup;
import org.narrative.network.core.narrative.rewards.RewardPeriod;
import org.narrative.network.core.narrative.rewards.RewardPeriodStep;
import org.narrative.network.core.quartz.NetworkJob;
import org.narrative.network.core.quartz.services.QuartzJobScheduler;
import org.narrative.network.core.system.NetworkRegistry;
import org.narrative.network.shared.tasktypes.GlobalTaskImpl;
import org.narrative.network.shared.tasktypes.TaskRunner;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobBuilder;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.time.LocalDate;
import java.util.Date;

import static org.narrative.common.util.CoreUtils.*;
import static org.quartz.TriggerBuilder.*;

/**
 * Date: 2019-05-22
 * Time: 08:48
 *
 * @author jonmark
 */
@DisallowConcurrentExecution
public class ProcessRewardPeriodJob extends NetworkJob {

    private static final String REWARD_PERIOD_OID = "rewardPeriodOid";

    @Override
    protected void executeJob(JobExecutionContext context) throws JobExecutionException {
        OID rewardOid = getOidFromContext(context, REWARD_PERIOD_OID);

        assert rewardOid != null : "Should always have a rewardOid";

        RewardPeriod period = RewardPeriod.dao().get(rewardOid);

        // jw: the ProcessRewardPeriodTask will do all validation on the period that we just tried to load.
        // jw: it's pretty much the same validation we did when scheduling this job in the first place.
        try {
            getNetworkContext().doAreaTask(Area.dao().getNarrativePlatformArea(), new ProcessRewardPeriodTask(period));

            // bl: make sure we flush all sessions so that any potential errors are captured now.
            PartitionGroup.getCurrentPartitionGroup().flushAllSessions();

            // bl: look to see if there is another reward period to process.
            // bl: intentionally doing this here instead of in ProcessRewardPeriodTask since ProcessRewardPeriodTask
            // is used during the install process to process old rewards. we only want to do this check when
            // we're processing rewards as part of this job.
            RewardPeriod nextRewardPeriodToProcess = RewardPeriod.dao().getOldestIncompleteRewardPeriodBefore(RewardUtils.nowYearMonth());
            if(exists(nextRewardPeriodToProcess)) {
                // bl: if we found another reward period that needs processing, then kick off a one-off instance
                // of the CheckRewardsProcessStatusJob to handle appropriately
                JobBuilder builder = QuartzJobScheduler.createRecoverableJobBuilder(CheckRewardsProcessStatusJob.class, "-OneOff");
                QuartzJobScheduler.GLOBAL.schedule(builder);
            }

        } catch(Exception e) {
            // jw: let's try and reschedule this to try again later.
            TaskRunner.doRootGlobalTask(new GlobalTaskImpl<Object>(true) {
                @Override
                protected Object doMonitoredTask() {
                    RewardPeriod period = RewardPeriod.dao().get(rewardOid);

                    schedule(period, true, null);
                    return null;
                }
            });

            // bl: send an email to alert us to the failure
            NetworkRegistry.getInstance().sendDevOpsStatusEmail("Rewards Processing Failure", "Rewards failed to process.\n" +
                    "Reference ID: " + NetworkRegistry.getInstance().getReferenceIdFromException(e) + "\n" +
                    "Exception: " + Debug.stackTraceFromException(e));

            // jw: with that behind us let's let the error continue on and revert the current transaction
            throw e;
        }
    }

    public static void schedule(RewardPeriod period) {
        schedule(period, false, null);
    }

    // todo:post-v1.2.3 make this private and remove errorOffset param. only needed for the ScheduleIncompleteRewardPeriodJob
    public static void schedule(RewardPeriod period, boolean forError, Long errorOffset) {
        assert exists(period) : "The period should always be provided!";
        assert !period.isCompleted() : "The period should not have already completed!";
        assert forError || !period.getCompletedSteps().contains(RewardPeriodStep.SCHEDULE_PROCESSING_JOB) : "We should never try and schedule a period that has already been scheduled. That previous job should execute until the period is complete!";

        // jw: we should only ever have one of these, so let's use a single name.
        // bl: when we are re-triggering a job for error, let's use an OID in the name to differentiate it.
        // otherwise, we'll get errors like: Unable to store Job : 'DEFAULT.ProcessRewardPeriodJob-RewardPeriod', because one already exists with this identification.
        JobBuilder builder = QuartzJobScheduler.createRecoverableJobBuilder(ProcessRewardPeriodJob.class, "-RewardPeriod" + (forError ? "-" + OIDGenerator.getNextOID() : ""))
                .usingJobData(REWARD_PERIOD_OID, period.getOid().getValue())
                ;

        if (forError) {
            // jw: for dev environments let's give one minute between retries. This should make testing easier.
            long offset = errorOffset!=null ? errorOffset : NetworkRegistry.getInstance().isLocalOrDevServer()
                    ? IPDateUtil.MINUTE_IN_MS
                    // jw: for staging and production let's provide 30 minutes.
                    : IPDateUtil.MINUTE_IN_MS * 30;

            QuartzJobScheduler.GLOBAL.schedule(
                    builder,
                    newTrigger().startAt(new Date(System.currentTimeMillis()+offset))
            );

        // jw: if we are scheduling for a period older than last month, or we are after the fifth then let's just run it now.
        } else if (RewardUtils.nowYearMonth().minusMonths(1).isAfter(period.getPeriod()) || LocalDate.now(RewardUtils.REWARDS_ZONE_OFFSET).getDayOfMonth() >= RewardUtils.REWARD_DAY_OF_MONTH) {
            QuartzJobScheduler.GLOBAL.schedule(builder);

        // jw: otherwise, let's schedule it for the 5th.
        } else {
            QuartzJobScheduler.GLOBAL.schedule(
                    builder,
                    newTrigger().startAt(new Date(LocalDate.now().withDayOfMonth(RewardUtils.REWARD_DAY_OF_MONTH).atStartOfDay().toInstant(RewardUtils.REWARDS_ZONE_OFFSET).toEpochMilli()))
            );
        }

        period.getCompletedSteps().add(RewardPeriodStep.SCHEDULE_PROCESSING_JOB);
    }
}
