package org.narrative.network.customizations.narrative.service.impl.file;

import org.narrative.common.persistence.OID;
import org.narrative.network.core.fileondisk.base.FileOnDisk;
import org.narrative.network.core.quartz.NetworkJob;
import org.narrative.network.shared.tasktypes.GlobalTaskImpl;
import org.narrative.network.shared.tasktypes.TaskRunner;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

/**
 * Date: 2019-08-20
 * Time: 10:12
 *
 * @author brian
 */
@DisallowConcurrentExecution
public class CleanupOldTempFilesJob extends NetworkJob {
    private static final Duration TEMP_FILE_LIFETIME = Duration.ofDays(1);

    @Override
    protected void executeJob(JobExecutionContext context) throws JobExecutionException {
        Timestamp olderThan = new Timestamp(Instant.now().minus(TEMP_FILE_LIFETIME).toEpochMilli());
        List<OID> fileOnDiskOidsToDelete = FileOnDisk.dao().getExpiredTempFileOids(olderThan);
        for (OID fileOnDiskOid : fileOnDiskOidsToDelete) {
            TaskRunner.doRootGlobalTask(new GlobalTaskImpl<Object>() {
                @Override
                protected Object doMonitoredTask() {
                    FileOnDisk fileOnDisk = FileOnDisk.dao().get(fileOnDiskOid);
                    FileOnDisk.dao().delete(fileOnDisk);
                    return null;
                }
            });
        }
    }
}
