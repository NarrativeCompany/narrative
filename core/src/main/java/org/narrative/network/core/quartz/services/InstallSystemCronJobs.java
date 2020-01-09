package org.narrative.network.core.quartz.services;

import org.narrative.common.util.QuartzUtil;
import org.narrative.network.core.area.base.services.RemoveStaleItemHourTrendingStatsJob;
import org.narrative.network.core.content.base.services.CalculateTrendingContentJob;
import org.narrative.network.core.narrative.rewards.services.CheckRewardsProcessStatusJob;
import org.narrative.network.core.narrative.rewards.services.RewardUtils;
import org.narrative.network.core.quartz.NetworkJob;
import org.narrative.network.core.search.IndexType;
import org.narrative.network.core.search.services.AuditSearchIndexMissingItems;
import org.narrative.network.core.search.services.IndexHandlerJobBase;
import org.narrative.network.core.search.services.IndexOperationJob;
import org.narrative.network.core.search.services.OptimizeIndexes;
import org.narrative.network.core.user.services.CleanupExpiredPendingEmailAddressesJob;
import org.narrative.network.customizations.narrative.niches.nicheauction.services.ProcessAuctionBeginningPaymentPeriodJob;
import org.narrative.network.customizations.narrative.niches.nicheauction.services.ProcessExpiredInvoicesJob;
import org.narrative.network.customizations.narrative.niches.services.ReleaseRejectedNicheNamesJob;
import org.narrative.network.customizations.narrative.payments.services.DetectNicheAuctionInvoicePaymentsOnNeoscanJob;
import org.narrative.network.customizations.narrative.payments.services.DetectPublicationInvoicePaymentsOnNeoscanJob;
import org.narrative.network.customizations.narrative.posts.services.CleanupEmptyDraftPostsJob;
import org.narrative.network.customizations.narrative.publications.services.DeleteExpiredPublicationsJob;
import org.narrative.network.customizations.narrative.publications.services.ProcessPublicationExpiringJob;
import org.narrative.network.customizations.narrative.reputation.services.ProcessSentReputationEventsJob;
import org.narrative.network.customizations.narrative.reputation.services.SendConductNegativeEmailsJob;
import org.narrative.network.customizations.narrative.reputation.services.SendQueuedReputationEventsJob;
import org.narrative.network.customizations.narrative.service.impl.file.CleanupOldTempFilesJob;
import org.narrative.network.customizations.narrative.services.CurrentNrveUsdValueCachingJob;
import org.narrative.network.shared.tasktypes.GlobalTaskImpl;
import org.narrative.network.shared.tasktypes.TaskRunner;
import org.narrative.network.shared.util.NetworkLogger;
import org.quartz.CronTrigger;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

/**
 * User: barry
 * Date: Mar 10, 2010
 * Time: 11:47:53 AM
 * <p>
 * In theory this could be modified to run every time at startup to ensure the system cron jobs are present.
 */
public class InstallSystemCronJobs extends GlobalTaskImpl<Object> {
    private static final NetworkLogger logger = new NetworkLogger(InstallSystemCronJobs.class);
    private Set<String> currentTriggerNames;
    private Set<String> currentJobNames;

    @Override
    protected Object doMonitoredTask() {
        currentTriggerNames = new HashSet<>(QuartzJobScheduler.GLOBAL.getTriggerNames(null));
        cronStyleJobs();

        //bk: This logic is not quite right as other jobs (not the cron ones below) will potentially be here as well
        // bl: not only is it "not quite right" but it has a terrible side effect of deleting jobs that we need
        // and want to have! removing this. if you want to remove a job, it is your responsibility to explicitly
        // delete the job from quartz as well as any triggers. we won't clean up automatically for you.
        /*for (String triggerName : currentTriggerNames) {
            Trigger trigger = QuartzJobScheduler.GLOBAL.getTrigger(triggerName);
            if(trigger==null || trigger.getFinalFireTime()==null) {
                logger.info("Removing unused trigger: "+triggerName);
                QuartzJobScheduler.GLOBAL.removeTrigger(triggerName, null);
            }
        }*/

        currentJobNames = new HashSet<>(QuartzJobScheduler.GLOBAL.getJobNames());
        durableJobs();
        // bl: similar to triggers above, we won't automatically delete jobs, either.
        /*for (String currentJobName : currentJobNames) {
            JobDetail jobDetail = QuartzJobScheduler.GLOBAL.getJobDetails(currentJobName);
            if(jobDetail==null || jobDetail.isDurable()) {
                QuartzJobScheduler.GLOBAL.remove(currentJobName);
            }
        }*/

        return null;
    }

    private void cronStyleJobs() {
        // stale ItemHourTrendingStats cleanup scheduled task to run once a day at 2:45 AM
        scheduleIfNecessary(RemoveStaleItemHourTrendingStatsJob.class, QuartzUtil.makeDailyTrigger(2, 45));

        // cleanup expired Email Addresses, every minute
        scheduleIfNecessary(CleanupExpiredPendingEmailAddressesJob.class, QuartzUtil.makeMinutelyTrigger(1));

        // recalculate the USD NRVE value every 5 minutes
        scheduleIfNecessary(CurrentNrveUsdValueCachingJob.class, QuartzUtil.makeMinutelyTrigger(5));

        // detect niche and publication payments via Neoscan's API every minute
        scheduleIfNecessary(DetectNicheAuctionInvoicePaymentsOnNeoscanJob.class, QuartzUtil.makeMinutelyTrigger(1));
        scheduleIfNecessary(DetectPublicationInvoicePaymentsOnNeoscanJob.class, QuartzUtil.makeMinutelyTrigger(1));

        // send any queued ReputationEvent objects to the reputation-module
        scheduleIfNecessary(SendQueuedReputationEventsJob.class, QuartzUtil.makeMinutelyTrigger(1));

        // process sent ReputationEvent objects from the reputation-module
        scheduleIfNecessary(ProcessSentReputationEventsJob.class, QuartzUtil.makeMinutelyTrigger(1));

        // cleanup stale empty Narrative Post drafts
        scheduleIfNecessary(CleanupEmptyDraftPostsJob.class, QuartzUtil.makeHourlyTrigger(1));

        // cleanup expired invoices
        // jw: yes, we want this to cycle every minute.
        scheduleIfNecessary(ProcessExpiredInvoicesJob.class, QuartzUtil.makeMinutelyTrigger(1));

        // delete expired publications
        scheduleIfNecessary(DeleteExpiredPublicationsJob.class, QuartzUtil.makeHourlyTrigger(1));

        // Send reputation emails
        scheduleIfNecessary(SendConductNegativeEmailsJob.class, QuartzUtil.makeMinutelyTrigger(1));

        // Calculate TrendingContent Stats every five minutes.
        scheduleIfNecessary(CalculateTrendingContentJob.class, QuartzUtil.makeMinutelyTrigger(5));

        // release rejected Niche names every hour
        scheduleIfNecessary(ReleaseRejectedNicheNamesJob.class, QuartzUtil.makeHourlyTrigger(1));

        // monthly rewards status check. should run the day after rewards should have been processed.
        scheduleIfNecessary(CheckRewardsProcessStatusJob.class, QuartzUtil.makeMonthlyTrigger(RewardUtils.REWARD_DAY_OF_MONTH+1, 0, 0, RewardUtils.REWARDS_TIME_ZONE));

        // delete expired temp files every 5 minutes
        scheduleIfNecessary(CleanupOldTempFilesJob.class, QuartzUtil.makeMinutelyTrigger(5));

        {
            JobBuilder jobBuilder = QuartzJobScheduler.createRecoverableJobBuilder(AuditSearchIndexMissingItems.class);
            IndexHandlerJobBase.storeIndexTypesSet(jobBuilder, EnumSet.copyOf(IndexType.getAllIndexTypes()));

            // do it every day at 3AM
            scheduleIfNecessary(jobBuilder, QuartzUtil.makeDailyTrigger(3, 0));
        }

        {
            JobBuilder jobBuilder = QuartzJobScheduler.createRecoverableJobBuilder(OptimizeIndexes.class);

            // jw: do it every day at 3:30AM
            scheduleIfNecessary(jobBuilder, QuartzUtil.makeDailyTrigger(3, 30));
        }
    }

    private void durableJobs() {
        addDurableJobIfNecessary(ProcessAuctionBeginningPaymentPeriodJob.class);

        addDurableJobIfNecessary(IndexOperationJob.class);

        addDurableJobIfNecessary(ProcessPublicationExpiringJob.class);
    }

    private void addDurableJobIfNecessary(Class<? extends NetworkJob> clz) {
        if (currentJobNames.contains(clz.getSimpleName())) {
            currentJobNames.remove(clz.getSimpleName());
            logger.info("Already existing durable job: " + clz.getSimpleName());
        } else {
            JobBuilder jobBuilder = QuartzJobScheduler.createRecoverableJobBuilder(clz);
            QuartzJobScheduler.GLOBAL.addDurableJob(jobBuilder);
            logger.info("Added durable job: " + clz.getSimpleName());
        }
    }

    private void scheduleIfNecessary(Class<? extends Job> clz, TriggerBuilder triggerBuilder) {
        JobBuilder jobBuilder = QuartzJobScheduler.createRecoverableJobBuilder(clz);
        scheduleIfNecessary(jobBuilder, triggerBuilder);
    }

    private void scheduleIfNecessary(JobBuilder jobBuilder, TriggerBuilder triggerBuilder) {
        String triggerName = QuartzJobScheduler.setTriggerName(jobBuilder, triggerBuilder);
        if (currentTriggerNames.contains(triggerName)) {
            Trigger trigger = QuartzJobScheduler.GLOBAL.getTrigger(triggerName);
            Trigger newTrigger = triggerBuilder.build();

            // bl: we also should check to see if the job has the proper trigger so that when we update
            // triggers, they will actually take effect.
            if(areTriggersEquivalent(trigger, newTrigger)) {
                currentTriggerNames.remove(triggerName);
                logger.info("Already existing trigger job: " + triggerName);
                return;
            }
            // bl: replace the trigger!
            QuartzJobScheduler.GLOBAL.reschedule(trigger.getKey(), triggerBuilder);
            logger.info("Updated trigger job schedule: " + triggerName);
            return;
        }
        QuartzJobScheduler.GLOBAL.schedule(jobBuilder, triggerBuilder);
        logger.info("Added trigger job: " + triggerName);
    }

    private static boolean areTriggersEquivalent(Trigger trigger1, Trigger trigger2) {
        // bl: first and foremost, the trigger class must be the same (e.g. simple vs. cron)
        if(!trigger1.getClass().equals(trigger2.getClass())) {
            return false;
        }
        if(trigger1 instanceof SimpleTrigger) {
            SimpleTrigger simpleTrigger1 = (SimpleTrigger)trigger1;
            SimpleTrigger simpleTrigger2 = (SimpleTrigger)trigger2;
            assert simpleTrigger1.getRepeatCount() == -1 : "Found a simple trigger with limited repeat count! This isn't supported. TriggerKey/" + simpleTrigger1.getKey() + " repeatCount/" + simpleTrigger1.getRepeatCount();
            assert simpleTrigger2.getRepeatCount() == -1 : "Found a new simple trigger with limited repeat count! This isn't supported. TriggerKey/" + simpleTrigger2.getKey() + " repeatCount/" + simpleTrigger2.getRepeatCount();
            // bl: simple triggers are the same if they have the same repeat interval
            return simpleTrigger1.getRepeatInterval() == simpleTrigger2.getRepeatInterval();
        }
        assert trigger1 instanceof CronTrigger : "Currently only support SimpleTrigger and CronTrigger, not cls/" + trigger1.getClass().getName();
        // bl: cron triggers are teh same if they have the same cron expression
        return ((CronTrigger)trigger1).getCronExpression().equals(((CronTrigger)trigger2).getCronExpression());
    }

    public static void initializeSystemCronJobs() {
        TaskRunner.doRootGlobalTask(new InstallSystemCronJobs());
    }
}
