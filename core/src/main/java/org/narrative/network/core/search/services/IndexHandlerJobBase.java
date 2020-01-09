package org.narrative.network.core.search.services;

import org.narrative.common.util.SerializationUtil;
import org.narrative.network.core.quartz.NetworkJob;
import org.narrative.network.core.search.IndexType;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobBuilder;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.PersistJobDataAfterExecution;

import java.util.Set;

/**
 * Date: Sep 22, 2008
 * Time: 3:27:39 PM
 *
 * @author brian
 */
@PersistJobDataAfterExecution
@DisallowConcurrentExecution
public abstract class IndexHandlerJobBase extends NetworkJob {

    private static final String INDEX_TYPES_PROP = "INDEX_TYPES";
    private Set<IndexType> indexTypes;

    @Deprecated // Quartz Only
    public IndexHandlerJobBase() {
        super(false);
    }

    @Override
    protected void executeJob(JobExecutionContext context) throws JobExecutionException {
        indexTypes = SerializationUtil.deserializeCollection(context.getMergedJobDataMap().getString(INDEX_TYPES_PROP), Set.class, IndexType.class);
        executeIndexHandlerJob();
    }

    protected abstract void executeIndexHandlerJob() throws JobExecutionException;

    public Set<IndexType> getIndexTypes() {
        return indexTypes;
    }

    public static void storeIndexTypesSet(JobBuilder jobBuilder, Set<IndexType> indexTypesSet) {
        jobBuilder.usingJobData(INDEX_TYPES_PROP, SerializationUtil.serializeCollection(indexTypesSet));
    }
}
