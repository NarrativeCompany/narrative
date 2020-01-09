package org.narrative.network.core.cluster.actions.server;

import org.narrative.common.persistence.ObjectPair;
import org.narrative.network.core.quartz.NetworkJob;
import org.narrative.network.core.quartz.services.QuartzJobScheduler;
import org.quartz.JobExecutionContext;
import org.quartz.Scheduler;
import org.quartz.Trigger;

import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * User: barry
 * Date: Mar 9, 2010
 * Time: 10:55:05 AM
 */
public class QuartzJobStatusAction extends SystemMonitoringAction {

    Set<QuartzJobInfo> localQuartzJobsQueue;
    Set<QuartzJobInfo> globalQuartzJobsQueue;

    public String input() throws Exception {
        localQuartzJobsQueue = doQuartzJobResearch(QuartzJobScheduler.LOCAL);
        globalQuartzJobsQueue = doQuartzJobResearch(QuartzJobScheduler.GLOBAL);

        return INPUT;
    }

    private Set<QuartzJobInfo> doQuartzJobResearch(QuartzJobScheduler quartzJobScheduler) {
        Map<ObjectPair<String, String>, QuartzJobInfo> jobInfoMap = new HashMap<ObjectPair<String, String>, QuartzJobInfo>();

        for (String triggerGroupName : quartzJobScheduler.getTriggerGroupNames()) {
            // bl: let's skip recovering jobs. just want to look for normal triggers that have been scheduled.
            if (Scheduler.DEFAULT_RECOVERY_GROUP.equals(triggerGroupName)) {
                continue;
            }
            for (String triggerName : quartzJobScheduler.getTriggerNames(triggerGroupName)) {
                Trigger trigger = quartzJobScheduler.getTrigger(triggerName, triggerGroupName);
                // bl: avoid race conditions by skipping triggers and jobDetails that existed at the start of the loop,
                // but no longer exist when we are iterating through the loop options.
                if (trigger == null) {
                    continue;
                }
                QuartzJobInfo quartzJobInfo = new QuartzJobInfo(trigger);
                ObjectPair<String, String> key = new ObjectPair<String, String>(triggerGroupName, trigger.getJobKey().getName());
                jobInfoMap.put(key, quartzJobInfo);
            }
        }

        for (JobExecutionContext jobExecutionContext : (List<JobExecutionContext>) quartzJobScheduler.getCurrentlyExecutingJobs()) {
            Trigger trigger = jobExecutionContext.getTrigger();
            ObjectPair<String, String> key = new ObjectPair<String, String>(trigger.getJobKey().getGroup(), trigger.getJobKey().getName());
            if (!jobInfoMap.containsKey(key)) {
                QuartzJobInfo quartzJobInfo = new QuartzJobInfo(trigger);
                jobInfoMap.put(key, quartzJobInfo);
            }
            jobInfoMap.get(key).addJobExecutionContext(jobExecutionContext);
        }

        TreeSet<QuartzJobInfo> output = new TreeSet<QuartzJobInfo>(new Comparator<QuartzJobInfo>() {
            @Override
            public int compare(QuartzJobInfo o1, QuartzJobInfo o2) {
                if (o1.isRunning() && o2.isRunning()) {
                    return o1.getFireTime().compareTo(o2.getFireTime());
                }

                if (o1.isRunning()) {
                    return -1;
                }

                if (o2.isRunning()) {
                    return 1;
                }

                if (o1.getNextFireTime() != null && o2.getNextFireTime() != null) {
                    return o1.getNextFireTime().compareTo(o2.getNextFireTime());
                }
                return 0;
            }
        });

        output.addAll(jobInfoMap.values());
        return output;
    }

    public Set<QuartzJobInfo> getLocalQuartzJobsQueue() {
        return localQuartzJobsQueue;
    }

    public Set<QuartzJobInfo> getGlobalQuartzJobsQueue() {
        return globalQuartzJobsQueue;
    }

    public static class QuartzJobInfo {
        final String jobName;
        final String triggerGroupName;
        final String triggerName;
        final Date previousFireTime;
        final Date nextFireTime;

        Date fireTime = null;
        JobExecutionContext jobExecutionContext = null;

        public QuartzJobInfo(Trigger trigger) {
            jobName = trigger.getJobKey().getName();
            triggerGroupName = trigger.getJobKey().getGroup();
            triggerName = trigger.getJobKey().getName();
            previousFireTime = trigger.getPreviousFireTime();
            nextFireTime = trigger.getNextFireTime();
        }

        public void addJobExecutionContext(JobExecutionContext jobExecutionContext) {
            this.jobExecutionContext = jobExecutionContext;
        }

        public String getJobStatus() {
            if (jobExecutionContext != null) {
                return (String) jobExecutionContext.get(NetworkJob.JOB_STATUS_MESSAGE);
            }
            return "Not Running";
        }

        public boolean isRunning() {
            return jobExecutionContext != null;
        }

        public String getJobName() {
            return jobName;
        }

        public String getTriggerGroupName() {
            return triggerGroupName;
        }

        public String getTriggerName() {
            return triggerName;
        }

        public Date getPreviousFireTime() {
            return previousFireTime;
        }

        public Date getNextFireTime() {
            return nextFireTime;
        }

        public Date getFireTime() {
            if (fireTime == null && jobExecutionContext != null) {
                fireTime = jobExecutionContext.getFireTime();
            }
            return fireTime;
        }
    }
}
