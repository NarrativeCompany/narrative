package org.narrative.network.core.search.services;

import org.narrative.network.core.quartz.NetworkJob;
import org.narrative.network.core.search.IndexHandler;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * User: barry
 * Date: Feb 23, 2010
 * Time: 11:14:12 AM
 */
@DisallowConcurrentExecution
public class OptimizeIndexes extends NetworkJob {
    @Deprecated // Quartz Only
    public OptimizeIndexes() { }

    @Override
    protected void executeJob(JobExecutionContext context) throws JobExecutionException {
        // jw: Lets trigger a optimize on the solr server.  This will optimize all documents and indexes, and only needs
        //     to be called once.
        IndexHandler.optimizeSolrServer();
    }
}
