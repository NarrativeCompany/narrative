package org.narrative.network.core.user.services;

import org.narrative.network.core.quartz.NetworkJob;
import org.narrative.network.core.user.EmailAddress;
import org.narrative.network.shared.util.NetworkLogger;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.time.Instant;

/**
 * Date: 2019-07-15
 * Time: 08:15
 *
 * @author jonmark
 */
@DisallowConcurrentExecution
public class CleanupExpiredPendingEmailAddressesJob extends NetworkJob {
    private static final NetworkLogger logger = new NetworkLogger(CleanupExpiredPendingEmailAddressesJob.class);

    @Override
    protected void executeJob(JobExecutionContext context) throws JobExecutionException {
        int deleted = EmailAddress.dao().deleteExpiredPendingEmailAddresses(Instant.now().minus(EmailAddress.EMAIL_CHANGE_CONFIRMATION_WINDOW));

        if (logger.isInfoEnabled()) {
            logger.info("Cleaned up " + deleted + " expired Pending Email Addresses.");
        }
    }
}
