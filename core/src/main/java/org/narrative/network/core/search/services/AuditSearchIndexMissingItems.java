package org.narrative.network.core.search.services;

import org.narrative.network.core.search.IndexHandler;
import org.narrative.network.core.search.IndexHandlerManager;
import org.narrative.network.core.search.IndexType;
import org.quartz.InterruptableJob;
import org.quartz.JobExecutionException;

/**
 * Date: Sep 18, 2008
 * Time: 11:52:27 AM
 *
 * @author brian
 */
public class AuditSearchIndexMissingItems extends IndexHandlerJobBase implements InterruptableJob {

    @Deprecated // Quartz Only
    public AuditSearchIndexMissingItems() { }

    @Override
    protected void executeIndexHandlerJob() throws JobExecutionException {
        for (IndexType indexType : getIndexTypes()) {
            IndexHandler handler = IndexHandlerManager.getIndexHandler(indexType);
            handler.auditAndFixMissingItems();
            checkIsInterrupted();
        }
    }
}
