package org.narrative.network.core.cluster.services;

import org.narrative.common.persistence.hibernate.DatabaseResources;
import org.narrative.network.core.cluster.partition.PartitionType;
import org.narrative.network.core.quartz.services.QuartzJobScheduler;
import org.narrative.network.core.statistics.StatisticManager;
import org.narrative.network.shared.tasktypes.GlobalTaskImpl;
import org.narrative.network.shared.util.NetworkLogger;
import org.quartz.JobDetail;
import org.quartz.Trigger;
import org.quartz.impl.jdbcjobstore.Constants;

/**
 * Quartz triggers can fail sometimes and get into a perpetual "ERROR" TRIGGER_STATE.
 * this task aims to look for any such triggers and "fix" them on software updates via a patch.
 *
 * Date: 2019-06-05
 * Time: 08:44
 *
 * @author brian
 */
public class RestartFailedQuartzTriggersTask extends GlobalTaskImpl<Object> {
    private static final NetworkLogger logger = new NetworkLogger(RestartFailedQuartzTriggersTask.class);
    @Override
    protected Object doMonitoredTask() {
        QuartzJobScheduler scheduler = QuartzJobScheduler.GLOBAL;

        for (String triggerGroupName : scheduler.getTriggerGroupNames()) {
            for (String triggerName : scheduler.getTriggerNames(triggerGroupName)) {
                try {
                    resumeTriggerIfNecessary(triggerName, triggerGroupName);
                } catch(Throwable t) {
                    StatisticManager.recordException(t, false, null);
                    if(logger.isErrorEnabled()) logger.error("Failed resuming Quartz trigger name/" + triggerName + " group/" + triggerGroupName, t);
                }
            }
        }

        return null;
    }

    private void resumeTriggerIfNecessary(String triggerName, String triggerGroupName) {
        QuartzJobScheduler scheduler = QuartzJobScheduler.GLOBAL;

        Trigger trigger = scheduler.getTrigger(triggerName, triggerGroupName);
        // bl: avoid race conditions by skipping triggers and jobDetails that existed at the start of the loop,
        // but no longer exist when we are iterating through the loop options.
        if (trigger == null) {
            return;
        }
        JobDetail jobDetail = scheduler.getJobDetails(trigger.getJobKey().getName());
        if (jobDetail == null) {
            return;
        }

        // bl: at this point, we just need to look for any triggers that are in an error state
        Trigger.TriggerState state = scheduler.getTriggerState(trigger.getKey());
        // bl: if the trigger is in error, we need to resume it!
        if (state == Trigger.TriggerState.ERROR) {
            // bl: resumeTrigger won't actually bring it out of error state. we have to actually
            // run SQL to fix the trigger
            //scheduler.resumeTrigger(trigger.getKey());
            DatabaseResources dr = PartitionType.GLOBAL.getSingletonPartition().getDatabaseResources();
            dr.executeStatementSafely(
                    String.format("update %s set %s=? where %s=? and %s=? and %s=? and %s=?",
                            Constants.DEFAULT_TABLE_PREFIX + Constants.TABLE_TRIGGERS,
                            Constants.COL_TRIGGER_STATE,
                            Constants.COL_SCHEDULER_NAME,
                            Constants.COL_TRIGGER_GROUP,
                            Constants.COL_TRIGGER_NAME,
                            Constants.COL_TRIGGER_STATE),
                    Constants.STATE_WAITING,
                    scheduler.getSchedulerName(),
                    triggerGroupName,
                    triggerName,
                    Constants.STATE_ERROR
            );
        }
    }
}
