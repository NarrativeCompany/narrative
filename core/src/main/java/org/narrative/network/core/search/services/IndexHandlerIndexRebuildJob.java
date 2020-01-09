package org.narrative.network.core.search.services;

import org.narrative.network.core.search.IndexHandler;
import org.narrative.network.core.search.IndexType;
import org.quartz.InterruptableJob;
import org.quartz.JobExecutionException;

/**
 * User: Paul
 * Date: Aug 1, 2008
 * Time: 3:24:54 PM
 */
public class IndexHandlerIndexRebuildJob extends IndexHandlerJobBase implements InterruptableJob {

    @Deprecated //Quartz Only
    public IndexHandlerIndexRebuildJob() {}

    @Override
    protected void executeIndexHandlerJob() throws JobExecutionException {
        for (IndexType indexType : getIndexTypes()) {
            IndexHandler handler = indexType.getIndexHandler();
            handler.rebuildIndex();
            checkIsInterrupted();
        }
    }

}
