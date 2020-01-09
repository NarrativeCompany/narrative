package org.narrative.network.customizations.narrative.reputation.services;

import org.narrative.common.persistence.OID;
import org.narrative.network.core.area.base.Area;
import org.narrative.network.core.quartz.NetworkJob;
import org.narrative.network.customizations.narrative.reputation.UserReputation;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.PersistJobDataAfterExecution;

import java.time.Instant;
import java.util.List;

@PersistJobDataAfterExecution
@DisallowConcurrentExecution
public class SendConductNegativeEmailsJob extends NetworkJob {

    private static final String CONDUCT_NEGATIVE_EMAIL_JOB_TIMESTAMP_IN_MILLIS_KEY = "conductNegativeEmailJobTimestampInMillis";

    @Override
    protected void executeJob(JobExecutionContext context) throws JobExecutionException {
        final JobDataMap jobDataMap = context.getJobDetail().getJobDataMap();

        // if this is the first time running, then just start looking for users from now going forward
        if (!jobDataMap.containsKey(CONDUCT_NEGATIVE_EMAIL_JOB_TIMESTAMP_IN_MILLIS_KEY)) {
            jobDataMap.put(CONDUCT_NEGATIVE_EMAIL_JOB_TIMESTAMP_IN_MILLIS_KEY, System.currentTimeMillis());
        }

        // Normal execution of the job
        // Get the start and end timestamps
        Instant startTimestamp = Instant.ofEpochMilli(jobDataMap.getLongValue(CONDUCT_NEGATIVE_EMAIL_JOB_TIMESTAMP_IN_MILLIS_KEY));
        Instant endTimestamp = Instant.now();

        // Put the end timestamp into the data map
        jobDataMap.put(CONDUCT_NEGATIVE_EMAIL_JOB_TIMESTAMP_IN_MILLIS_KEY, endTimestamp.toEpochMilli());

        Area narrativeArea = Area.dao().getNarrativePlatformArea();

        // Get users who have become conduct neutral between the last run and now
        List<OID> userOidsWhoBecameConductNeutralInWindow = UserReputation.dao().getUserOidsWhoBecameConductNeutralInWindow(startTimestamp, endTimestamp);

        // Send email to users who have become conduct neutral
        getNetworkContext().doAreaTask(narrativeArea, new SendConductNegativeEndedEmailTask(userOidsWhoBecameConductNeutralInWindow));

    }
}
