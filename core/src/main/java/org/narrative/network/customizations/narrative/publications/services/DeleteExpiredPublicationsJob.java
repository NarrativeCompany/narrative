package org.narrative.network.customizations.narrative.publications.services;

import org.narrative.common.persistence.OID;
import org.narrative.network.core.area.base.Area;
import org.narrative.network.core.quartz.NetworkJob;
import org.narrative.network.customizations.narrative.publications.Publication;
import org.narrative.network.shared.tasktypes.AreaTaskImpl;
import org.narrative.network.shared.tasktypes.TaskRunner;
import org.narrative.network.shared.util.NetworkLogger;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.util.List;

/**
 * Date: 2019-09-26
 * Time: 09:45
 *
 * @author jonmark
 */
@DisallowConcurrentExecution
public class DeleteExpiredPublicationsJob extends NetworkJob {
    private static final NetworkLogger logger = new NetworkLogger(DeleteExpiredPublicationsJob.class);

    @Override
    protected void executeJob(JobExecutionContext context) throws JobExecutionException {
        List<OID> publicationOids = Publication.dao().getDeletablePublicationOids();

        if (publicationOids.isEmpty()) {
            if (logger.isInfoEnabled()) {
                logger.info("No publications to process. Shorting out!");
            }
            return;
        }

        if (logger.isInfoEnabled()) {
            logger.info("Processing "+publicationOids.size()+" publications ready for deletion.");
        }

        int processed = 0;
        for (OID publicationOid : publicationOids) {
            // jw: this task should not happen often, so let's go ahead and log out each time.
            if (logger.isInfoEnabled()) {
                logger.info("Processing publication/"+publicationOid);
            }
            // jw: let's process each publication atomically.
            TaskRunner.doRootAreaTask(Area.dao().getNarrativePlatformArea().getOid(), new AreaTaskImpl<Object>() {
                @Override
                protected Object doMonitoredTask() {
                    getAreaContext().doAreaTask(new DeletePublicationTask(Publication.dao().get(publicationOid), false));
                    return null;
                }
            });
            processed++;
            // jw: log out every 10
            if (processed % 10 == 0) {
                if (logger.isInfoEnabled()) {
                    logger.info("Processed "+processed+" out of "+publicationOids.size()+" deletable publications.");
                }
            }
        }
    }
}
