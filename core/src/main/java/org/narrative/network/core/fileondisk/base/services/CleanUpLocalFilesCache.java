package org.narrative.network.core.fileondisk.base.services;

import org.narrative.common.util.IPIOUtil;
import org.narrative.network.core.fileondisk.base.services.filesystem.NetworkPath;
import org.narrative.network.core.quartz.NetworkJob;
import org.narrative.network.core.system.NetworkRegistry;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.io.File;

/**
 * Date: 4/9/13
 * Time: 10:37 AM
 *
 * @author brian
 */
@DisallowConcurrentExecution
public class CleanUpLocalFilesCache extends NetworkJob {
    @Override
    protected void executeJob(JobExecutionContext context) throws JobExecutionException {
        // bl: if this cluster doesn't support local files (e.g. it's a standalone), then we don't actually
        // need to do anything. certainly would NOT want to clean up old files from the remote_files
        // directory, which contains REAL data!
        assert NetworkPath.isLocalFileCacheSupportedByEnvironment() : "Should only run CleanUpLocalFilesCache job on clusters that have local files!";
        File localFileDir = NetworkPath.getLocalFileCacheDirectory();
        // bl: use nice on non-dev servers (since dev servers are OS X and probably won't have/need nice)
        IPIOUtil.pruneFilesByLastAccessedDateInDirectory(localFileDir, 7, !NetworkRegistry.getInstance().isLocalServer());
    }
}
