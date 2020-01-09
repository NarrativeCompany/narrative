package org.narrative.network.customizations.narrative.niches.services;

import org.narrative.network.core.area.base.Area;
import org.narrative.network.core.quartz.AreaJob;
import org.narrative.network.core.quartz.services.QuartzJobScheduler;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.TriggerBuilder;

/**
 * Created by IntelliJ IDEA.
 * User: jonmark
 * Date: 2/14/18
 * Time: 7:33 AM
 */
public abstract class NicheCustomizationAreaJobBase extends AreaJob {

    protected abstract void executeForNicheArea();

    protected void executeAreaJob(JobExecutionContext context) throws JobExecutionException {
        executeForNicheArea();
    }

    private static String getJobName(Class<? extends NicheCustomizationAreaJobBase> jobClass, Area area) {
        return jobClass.getSimpleName() + "/area/" + area.getOid();
    }

    protected static void registerForArea(Class<? extends NicheCustomizationAreaJobBase> cls, Area area, TriggerBuilder triggerBuilder, boolean unscheduleFirst) {
        if (unscheduleFirst) {
            unschedule(cls, area);
        }
        scheduleIfNecessary(cls, area, triggerBuilder);
    }

    protected static void unschedule(Class<? extends NicheCustomizationAreaJobBase> jobClass, Area area) {
        QuartzJobScheduler.GLOBAL.remove(getJobName(jobClass, area));
    }

    protected static void scheduleIfNecessary(Class<? extends NicheCustomizationAreaJobBase> jobClass, Area area, TriggerBuilder trigger) {
        JobDetail jobDetail = QuartzJobScheduler.GLOBAL.getJobDetails(getJobName(jobClass, area));
        if (jobDetail != null) {
            return;
        }
        JobBuilder jobBuilder = QuartzJobScheduler.createRecoverableJobBuilder(jobClass);
        QuartzJobScheduler.GLOBAL.scheduleAreaJobWithName(area, getJobName(jobClass, area), jobBuilder, trigger);
    }
}
