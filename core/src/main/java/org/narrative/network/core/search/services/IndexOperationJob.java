package org.narrative.network.core.search.services;

import org.narrative.common.persistence.OID;
import org.narrative.common.util.enums.EnumRegistry;
import org.narrative.network.core.quartz.NetworkJob;
import org.narrative.network.core.quartz.services.QuartzJobScheduler;
import org.narrative.network.core.search.IndexOperation;
import org.narrative.network.core.search.IndexOperationId;
import org.narrative.network.core.search.IndexType;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.TriggerBuilder;

import static org.quartz.TriggerBuilder.*;

/**
 * Date: 11/28/11
 * Time: 1:17 PM
 *
 * @author Jonmark Weber
 */
public class IndexOperationJob extends NetworkJob {
    private static String INDEX_TYPE = "indexType";
    private static String OPERATION_TYPE = "operationType";
    private static String OPERATION_OID = "operationOid";
    private static String OPERATION_EXTRA_DATA_OID = "operationExtraDataId";

    @Override
    protected void executeJob(JobExecutionContext context) throws JobExecutionException {
        IndexType indexType = EnumRegistry.getForId(IndexType.class, context.getMergedJobDataMap().getString(INDEX_TYPE), true);
        IndexOperation.Type operationType = EnumRegistry.getForId(IndexOperation.Type.class, context.getMergedJobDataMap().getInt(OPERATION_TYPE));
        OID operationOid = getOidFromContext(context, OPERATION_OID);
        OID operationExtraDataOid = getOidFromContext(context, OPERATION_EXTRA_DATA_OID);

        indexType.getIndexHandler().performOperation(new IndexOperation(operationType, new IndexOperationId(operationOid, operationExtraDataOid)));
    }

    public static void schedule(IndexType indexType, IndexOperation operation) {
        TriggerBuilder triggerBuilder = newTrigger().withIdentity(getTriggerName(indexType, operation)).forJob(IndexOperationJob.class.getSimpleName()).usingJobData(INDEX_TYPE, indexType.getIdStr()).usingJobData(OPERATION_TYPE, operation.getOperation().getId()).usingJobData(OPERATION_OID, operation.getId().getDocId().getValue()).usingJobData(OPERATION_EXTRA_DATA_OID, operation.getId().getExtraDataOid().getValue());

        QuartzJobScheduler.GLOBAL.schedule(triggerBuilder);
    }

    private static String getTriggerName(IndexType indexType, IndexOperation operation) {
        return IndexOperationJob.class.getSimpleName() + "/it/" + indexType + "/type/" + operation.getOperation() + "/id/" + operation.getId().getDocId() + "/edid/" + operation.getId().getExtraDataOid();
    }
}
