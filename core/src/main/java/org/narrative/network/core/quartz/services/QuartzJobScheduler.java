package org.narrative.network.core.quartz.services;

import org.narrative.common.persistence.OIDGenerator;
import org.narrative.common.util.IPDateUtil;
import org.narrative.common.util.IPUtil;
import org.narrative.common.util.UnexpectedError;
import org.narrative.network.core.area.base.Area;
import org.narrative.network.core.quartz.AreaJob;
import org.narrative.network.core.system.NetworkRegistry;
import org.narrative.network.shared.util.NetworkLogger;
import org.quartz.InterruptableJob;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.quartz.UnableToInterruptJobException;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.impl.matchers.GroupMatcher;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import static org.narrative.common.util.CoreUtils.*;
import static org.quartz.JobBuilder.*;
import static org.quartz.JobKey.DEFAULT_GROUP;
import static org.quartz.JobKey.*;
import static org.quartz.TriggerBuilder.*;
import static org.quartz.TriggerKey.*;

/**
 * User: barry
 * Date: Mar 3, 2010
 * Time: 11:50:52 AM
 */
public enum QuartzJobScheduler {
    GLOBAL("quartz-cluster.properties"),
    LOCAL("quartz-local.properties");

    private static final NetworkLogger logger = new NetworkLogger(QuartzJobScheduler.class);
    private final Scheduler scheduler;
    // bl: make life easier on developers doing email testing: don't use any delay for sending emails on dev servers.
    public static final long FIVE_MINUTES = NetworkRegistry.getInstance().isLocalServer() ? 0 : IPDateUtil.MINUTE_IN_MS * 5;

    private QuartzJobScheduler(String filename) {
        try {
            //This will get the "default" scheduler that is defined in the quartz.properties which is the clustered version
            StdSchedulerFactory stdSchedulerFactory = new StdSchedulerFactory(filename);
            scheduler = stdSchedulerFactory.getScheduler();
        } catch (SchedulerException e) {
            throw UnexpectedError.getRuntimeException("Could not start quartz scheduler with file: " + filename, e);
        }

        IPUtil.EndOfX.endOfApp.addRunnable("96QuartzScheduler" + this, new Runnable() {
            public void run() {
                try {
                    for (JobExecutionContext jobExecutionContext : scheduler.getCurrentlyExecutingJobs()) {
                        if (!InterruptableJob.class.isAssignableFrom(jobExecutionContext.getJobDetail().getJobClass())) {
                            logger.info("Quartz shutdown: can't interrupt job " + jobExecutionContext.getJobDetail().getKey().getName() + " as not InterruptableJob");
                            continue;
                        }
                        try {
                            scheduler.interrupt(jobExecutionContext.getJobDetail().getKey());
                            logger.info("Quartz shutdown: issued interrupt of job " + jobExecutionContext.getJobDetail().getKey().getName());
                        } catch (UnableToInterruptJobException e) {
                            // bl: in general, these interrupts should always succeed, so treat an UnableToInterruptJobException as an error.
                            logger.error("Quartz shutdown: failed to issue interrupt of job " + jobExecutionContext.getJobDetail().getKey().getName(), e);
                        }
                    }

                    scheduler.shutdown(true);
                } catch (SchedulerException e) {
                    throw UnexpectedError.getRuntimeException("Could not shutdown quartz scheduler.-" + this, e);
                }
            }
        });
    }

    //bk: Starting the QuartzScheduler
    public void start() {
        if (logger.isInfoEnabled()) {
            logger.info("Starting Quartz Scheduler");
        }
        try {
            scheduler.start();
        } catch (SchedulerException e) {
            throw UnexpectedError.getRuntimeException("Could not start quartz scheduler.-" + this, e);
        }
    }

    public void schedule(JobBuilder jobBuilder, TriggerBuilder triggerBuilder) {
        //this ensures the trigger has a name
        setTriggerName(jobBuilder, triggerBuilder);

        JobDetail jobDetail = jobBuilder.build();
        Trigger trigger = triggerBuilder.build();
        try {
            scheduler.scheduleJob(jobDetail, trigger);
        } catch (SchedulerException e) {
            throw UnexpectedError.getRuntimeException("Error scheduling job: " + jobDetail.getKey().getName() + " trigger: " + trigger.getKey().getName(), e);
        }
    }

    public void schedule(TriggerBuilder triggerBuilder) {
        Trigger trigger = triggerBuilder.build();
        try {
            scheduler.scheduleJob(trigger);
        } catch (SchedulerException e) {
            throw UnexpectedError.getRuntimeException("Error scheduling job with trigger: " + trigger.getKey().getName(), e);
        }
    }

    public void scheduleForFiveMinutesFromNow(JobBuilder jobBuilder) {
        scheduleForFiveMinutesFromNow("", jobBuilder);
    }

    public void scheduleForFiveMinutesFromNow(String name, JobBuilder jobBuilder) {
        String jobName = jobBuilder.build().getKey().getName() + name + "/" + OIDGenerator.getNextOID();
        jobBuilder.withIdentity(jobName);
        schedule(jobBuilder, newTrigger().startAt(new Date(System.currentTimeMillis() + FIVE_MINUTES)));
    }

    public void schedule(JobBuilder jobBuilder) {
        schedule("", jobBuilder);
    }

    public void schedule(String name, JobBuilder jobBuilder) {
        schedule(name, jobBuilder, newTrigger());
    }

    public void schedule(String name, JobBuilder jobBuilder, TriggerBuilder trigger) {
        scheduleWithName(jobBuilder.build().getKey().getName() + name + "/" + OIDGenerator.getNextOID(), jobBuilder, trigger);
    }

    private void scheduleWithName(String jobName, JobBuilder jobBuilder, TriggerBuilder trigger) {
        jobBuilder.withIdentity(jobName);
        schedule(jobBuilder, trigger);
    }

    public void schedule(String name, JobBuilder jobBuilder, Date date) {
        String jobName = jobBuilder.build().getKey().getName() + name + "/" + OIDGenerator.getNextOID();
        jobBuilder.withIdentity(jobName);
        schedule(jobBuilder, newTrigger().startAt(date));
    }

    public void scheduleAreaJob(Area area, String name, JobBuilder jobBuilder) {
        AreaJob.addAreaToJobDataMap(area, jobBuilder);
        schedule(name, jobBuilder);
    }

    public void scheduleAreaJobWithName(Area area, String jobName, JobBuilder jobBuilder, TriggerBuilder trigger) {
        AreaJob.addAreaToJobDataMap(area, jobBuilder);
        scheduleWithName(jobName, jobBuilder, trigger);
    }

    public boolean remove(String jobName) {
        try {
            return scheduler.deleteJob(jobKey(jobName));
        } catch (SchedulerException e) {
            throw UnexpectedError.getRuntimeException("Failed removing job: " + jobName, e);
        }
    }

    public boolean removeJob(Class<? extends Job> clz, String jobNameSuffix) {
        String jobName = getJobIdentity(clz, jobNameSuffix);
        try {
            return scheduler.deleteJob(jobKey(jobName));
        } catch (SchedulerException e) {
            throw UnexpectedError.getRuntimeException("Failed removing job: " + jobName, e);
        }
    }

    private static String getJobIdentity(Class<? extends Job> clz, String jobNameSuffix) {
        return clz.getSimpleName() + (jobNameSuffix == null ? "" : jobNameSuffix);
    }

    public boolean removeTrigger(String triggerName, String triggerGroup) {
        try {
            return scheduler.unscheduleJob(triggerKey(triggerName, triggerGroup));
        } catch (SchedulerException e) {
            throw UnexpectedError.getRuntimeException("Failed removing trigger: " + triggerName + " group: " + triggerGroup, e);
        }
    }

    public static JobBuilder createRecoverableJobBuilder(Class<? extends Job> clz) {
        return createRecoverableJobBuilder(clz, null);
    }

    public static JobBuilder createRecoverableJobBuilder(Class<? extends Job> clz, String jobNameSuffix) {
        return newJob().withIdentity(getJobIdentity(clz, jobNameSuffix)).ofType(clz).requestRecovery();
    }

    public List getCurrentlyExecutingJobs() {
        try {
            return scheduler.getCurrentlyExecutingJobs();
        } catch (SchedulerException e) {
            throw UnexpectedError.getRuntimeException("Could not get currently running jobs for " + name(), e);
        }
    }

    public List<String> getJobNames() {
        try {
            List<String> ret = new LinkedList<>();
            for (JobKey jobKey : scheduler.getJobKeys(GroupMatcher.groupEquals(DEFAULT_GROUP))) {
                ret.add(jobKey.getName());
            }
            return ret;
        } catch (SchedulerException e) {
            throw UnexpectedError.getRuntimeException("Could not get job names for " + name(), e);
        }
    }

    public JobDetail getJobDetails(String name) {
        try {
            return scheduler.getJobDetail(jobKey(name));
        } catch (SchedulerException e) {
            throw UnexpectedError.getRuntimeException("Could not get job details for " + name(), e);
        }
    }

    public List<String> getTriggerGroupNames() {
        try {
            return scheduler.getTriggerGroupNames();
        } catch (SchedulerException e) {
            throw UnexpectedError.getRuntimeException("Could not get trigger names for " + name(), e);
        }
    }

    public List<String> getTriggerNames(String triggerGroupName) {
        try {
            List<String> ret = new LinkedList<>();
            for (TriggerKey triggerKey : scheduler.getTriggerKeys(GroupMatcher.groupEquals(!isEmpty(triggerGroupName) ? triggerGroupName : DEFAULT_GROUP))) {
                ret.add(triggerKey.getName());
            }
            return ret;
        } catch (SchedulerException e) {
            throw UnexpectedError.getRuntimeException("Could not get trigger names for " + name(), e);
        }
    }

    public Trigger getTrigger(String triggerName) {
        return getTrigger(triggerName, null);
    }

    public Trigger getTrigger(String triggerName, String triggerGroup) {
        TriggerKey triggerKey = triggerKey(triggerName, triggerGroup);
        try {
            return scheduler.getTrigger(triggerKey);
        } catch (SchedulerException e) {
            throw UnexpectedError.getRuntimeException("Could not get trigger for " + name() + " key/" + triggerKey, e);
        }
    }

    public Date reschedule(TriggerKey oldTriggerKey, TriggerBuilder triggerBuilder) {
        Trigger trigger = triggerBuilder.build();
        try {
            return scheduler.rescheduleJob(oldTriggerKey, trigger);
        } catch (SchedulerException e) {
            throw UnexpectedError.getRuntimeException("Could not reschedule trigger for " + name() + " key/" + oldTriggerKey, e);
        }
    }

    public void addDurableJob(JobBuilder jobBuilder) {
        JobDetail jobDetail = jobBuilder.storeDurably().build();
        try {
            scheduler.addJob(jobDetail, false);
        } catch (SchedulerException e) {
            throw UnexpectedError.getRuntimeException("Could not add durable job " + jobDetail.getKey().getName(), e);
        }
    }

    public String getSchedulerName() {
        try {
            return scheduler.getSchedulerName();
        } catch (SchedulerException e) {
            throw UnexpectedError.getRuntimeException("Failed getting scheduler name for " + this, e);
        }
    }

    public Trigger.TriggerState getTriggerState(TriggerKey triggerKey) {
        try {
            return scheduler.getTriggerState(triggerKey);
        } catch (SchedulerException e) {
            throw UnexpectedError.getRuntimeException("Failed getting trigger state for triggerKey/" + triggerKey + " on scheduler/" + this, e);
        }
    }

    public void resumeTrigger(TriggerKey triggerKey) {
        try {
            scheduler.resumeTrigger(triggerKey);
        } catch (SchedulerException e) {
            throw UnexpectedError.getRuntimeException("Failed resuming trigger for triggerKey/" + triggerKey + " on scheduler/" + this, e);
        }
    }

    public static String setTriggerName(JobBuilder jobBuilder, TriggerBuilder triggerBuilder) {
        // bl: have to build a throwaway job here to get the job name
        String triggerName = jobBuilder.build().getKey().getName() + "Trigger";
        triggerBuilder.withIdentity(triggerName);
        return triggerName;
    }
}
