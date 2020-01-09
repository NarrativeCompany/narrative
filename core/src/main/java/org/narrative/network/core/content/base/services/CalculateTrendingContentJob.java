package org.narrative.network.core.content.base.services;

import org.narrative.common.util.UnexpectedError;
import org.narrative.network.core.content.base.TrendingContent;
import org.narrative.network.core.quartz.NetworkJob;
import org.narrative.network.core.settings.global.services.GlobalSettingsUtil;
import org.narrative.network.core.statistics.StatisticManager;
import org.narrative.network.shared.tasktypes.GlobalTaskImpl;
import org.narrative.network.shared.tasktypes.TaskRunner;
import org.narrative.network.shared.util.NetworkLogger;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import javax.validation.ConstraintViolationException;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Date: 2019-02-24
 * Time: 11:38
 *
 * @author jonmark
 */
@DisallowConcurrentExecution
public class CalculateTrendingContentJob extends NetworkJob {
    private static final NetworkLogger logger = new NetworkLogger(CalculateTrendingContentJob.class);

    @Deprecated // Quartz only
    public CalculateTrendingContentJob() { }

    @Override
    protected void executeJob(JobExecutionContext context) throws JobExecutionException {
        if (logger.isInfoEnabled()) {
            logger.info("Starting to recalculate TrendingContent!");
        }

        Instant previousBuildTime = GlobalSettingsUtil.getGlobalSettings().getCurrentTrendingContentBuildTime();
        Instant buildTime = Instant.now();

        if (buildTime.equals(previousBuildTime)) {
            String message = "WOW! somehow we ran in the same instant as the previous build time! epoch/"+buildTime.toEpochMilli()+". HOOOOOOOOOW?";
            logger.error(message);
            StatisticManager.recordException(UnexpectedError.getRuntimeException(message), false, null);
            return;
        }

        // jw: let's calculate the new TrendingContent in its own transaction.
        TaskRunner.doRootGlobalTask(new GlobalTaskImpl<Object>() {
            @Override
            protected Object doMonitoredTask() {
                if (logger.isInfoEnabled()) {
                    logger.info("Triggering recalc of TrendingContent for buildTime/"+buildTime.toEpochMilli());
                }

                // bl: need to attempt multiple times since there are race conditions if content is deleted
                // during the insert statement (after the select has already run). see #2634
                final int maxAttempts = 3;
                for(int i=0; i<maxAttempts; i++) {
                    try {
                        TrendingContent.dao().calculateTrendingContent(buildTime);
                        // if we succeeded, we're done!
                        break;
                    } catch(ConstraintViolationException e) {
                        // bl: if we hit our max attempts, then throw the exception. seems to be a lost cause.
                        if(i==(maxAttempts-1)) {
                            throw e;
                        }
                        // if we get the error, just try ignore and try again
                    }
                }

                GlobalSettingsUtil.getGlobalSettingsForWrite().setCurrentTrendingContentBuildTimeMs(buildTime.toEpochMilli());

                return null;
            }
        });

        // jw: now that the recalc is done, let's clear out all old records in a new transaction.
        TaskRunner.doRootGlobalTask(new GlobalTaskImpl<Object>() {
            @Override
            protected Object doMonitoredTask() {
                if (logger.isInfoEnabled()) {
                    logger.info("Clearing TrendingContent not for older than/"+buildTime.toEpochMilli());
                }

                // bl: in order to support load more for TrendingContent, let's only remove records that are over a day old.
                // that way, we can support load more with slightly-out-of-date TrendingContent build times.
                // after 24 hours, you will have to reload the page to get trending content from a different build time.
                TrendingContent.dao().deleteOldTrendingContent(buildTime.minus(1, ChronoUnit.DAYS));

                return null;
            }
        });
    }
}
