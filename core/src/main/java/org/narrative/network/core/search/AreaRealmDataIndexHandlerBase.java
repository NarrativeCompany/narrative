package org.narrative.network.core.search;

import org.narrative.common.persistence.OID;
import org.narrative.common.persistence.ObjectPair;
import org.narrative.common.util.SubListIterator;
import org.narrative.common.util.processes.GenericProcess;
import org.narrative.common.util.processes.ProcessManager;
import org.narrative.network.core.area.base.Area;
import org.narrative.network.core.user.AuthZone;
import org.narrative.network.core.rating.AgeRating;
import org.narrative.network.shared.tasktypes.AreaTaskImpl;
import org.narrative.network.shared.tasktypes.GlobalTaskImpl;
import org.narrative.network.shared.tasktypes.TaskRunner;
import org.narrative.network.shared.util.NetworkLogger;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;
import org.hibernate.ScrollableResults;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.narrative.common.util.CoreUtils.*;
import static org.narrative.network.shared.util.NetworkCoreUtils.*;

/**
 * Date: 2/16/11
 * Time: 9:25 AM
 *
 * @author brian
 */
public abstract class AreaRealmDataIndexHandlerBase extends AreaDataIndexHandlerBase {

    private static final NetworkLogger logger = new NetworkLogger(AreaRealmDataIndexHandlerBase.class);

    protected AreaRealmDataIndexHandlerBase(IndexType indexType) {
        super(indexType);
    }

    @Override
    public final void rebuildSolrIndex(final long indexVersion) {
        TaskRunner.doRootGlobalTask(new GlobalTaskImpl<Object>(false) {
            @Override
            protected Object doMonitoredTask() {
                getProcess().updateStatusMessageAndLog(logger, "Starting index rebuild for " + getIndexType() + " index");

                rebuildIndex(getProcess(), indexVersion);

                getProcess().updateStatusMessageAndLog(logger, "Finished index rebuild for " + getIndexType() + " index");

                return null;
            }
        });
    }

    protected abstract void rebuildIndex(GenericProcess process, long indexVersion);

    protected abstract ScrollableResults getAllIdsScrollable();

    public void auditAndFixMissingItems() {
        // this should do an audit and simply add items to the indexWriterBlockingQueue
        // items added to the queue (updates,deletes,add,optimize) will run and the
        // search index reader will be reloaded once it is done

        ProcessManager.getInstance().getCurrentProcess().updateStatusMessageAndLog(logger, "Starting index audit for missing items of type " + getIndexType());

        List<ObjectPair<OID, OID>> missingOids = getMissingOids();
        ProcessManager.getInstance().getCurrentProcess().updateStatusMessageAndLog(logger, "Found " + missingOids.size() + " missing items for " + getIndexType() + " index.");
        SubListIterator<ObjectPair<OID, OID>> iter = new SubListIterator<ObjectPair<OID, OID>>(missingOids, 10);

        while (iter.hasNext()) {
            List<ObjectPair<OID, OID>> oidsToDo = iter.next();
            for (ObjectPair<OID, OID> pair : oidsToDo) {
                OID oid = pair.getOne();
                OID areaOid = pair.getTwo();
                performOperation(IndexOperation.update(oid, areaOid));
            }
        }

        ProcessManager.getInstance().getCurrentProcess().updateStatusMessageAndLog(logger, "Finished index audit for " + getIndexType() + ". Added " + missingOids.size() + " missing items to " + getIndexType() + " index.");
    }

    private List<ObjectPair<OID, OID>> getMissingOids() {

        final List<ObjectPair<OID, OID>> oidsToAdd = new LinkedList<>();
        ScrollableResults results = getAllIdsScrollable();

        Collection<ObjectPair<OID, OID>> objectPairs = newHashSet();
        int rowsAudited = 0;
        while (results.next()) {
            rowsAudited++;
            objectPairs.add((ObjectPair<OID, OID>) results.get(0));

            if (rowsAudited % 500 == 0 && !objectPairs.isEmpty()) {
                Map<OID, ObjectPair<OID, OID>> oidToObjectPairs = ObjectPair.getAsMapOfOnesToObjs(objectPairs);
                for (OID oid : getIdsNotInIndex(oidToObjectPairs.keySet())) {
                    oidsToAdd.add(oidToObjectPairs.get(oid));
                }
                objectPairs.clear();
                if ((rowsAudited % 5000) == 0) {
                    ProcessManager.getInstance().getCurrentProcess().updateStatusMessageAndLog(logger, "Audit index missing items for " + rowsAudited + " " + getIndexType() + " items.");
                }
            }
        }
        if (!objectPairs.isEmpty()) {
            Map<OID, ObjectPair<OID, OID>> oidToObjectPairs = ObjectPair.getAsMapOfOnesToObjs(objectPairs);
            for (OID oid : getIdsNotInIndex(oidToObjectPairs.keySet())) {
                oidsToAdd.add(oidToObjectPairs.get(oid));
            }
        }

        return oidsToAdd;
    }

    protected abstract SolrInputDocument createSolrInputDocumentWithinArea(OID oid, long indexVersion, boolean forBulkOperation);

    @Override
    protected SolrInputDocument createSolrInputDocument(IndexOperationId id, final long indexVersion, final boolean forBulkOperation) {
        final OID oid = id.getDocId();

        return TaskRunner.doRootGlobalTask(new GlobalTaskImpl<SolrInputDocument>() {
            @Override
            protected SolrInputDocument doMonitoredTask() {
                Area area = Area.dao().getNarrativePlatformArea();
                return getNetworkContext().doAreaTask(area, new AreaTaskImpl<SolrInputDocument>(false) {
                    @Override
                    protected SolrInputDocument doMonitoredTask() {
                        return createSolrInputDocumentWithinArea(oid, indexVersion, forBulkOperation);
                    }
                });
            }
        });
    }

    protected SolrInputDocument createDataDocument(OID oid, OID userOid, String name, String fullText, Timestamp itemDate, Timestamp lastUpdateDate, AuthZone authZone, OID areaOid, AgeRating ageRating, long indexVersion) {
        return createAreaDataDocument(oid, userOid, name, fullText, itemDate, lastUpdateDate, authZone, areaOid, ageRating, indexVersion);
    }

    protected SolrInputDocument createDataDocument(OID oid, OID userOid, String name, String fullText, Timestamp itemDate, Timestamp lastUpdateDate, AuthZone authZone, OID areaOid, long indexVersion) {
        return createAreaDataDocument(oid, userOid, name, fullText, itemDate, lastUpdateDate, authZone, areaOid, null, indexVersion);
    }

    protected abstract SearchResult getResultFromSolrDocument(OID oid, int resultIndex, SolrDocument document);

    @Override
    protected SearchResult getResultFromSolrDocument(final SolrDocument document, final OID oid, final int resultIndex) {
        Area area = Area.dao().get(OID.valueOf(document.get(FIELD__COMMON__AREA_OID)));
        return networkContext().doAreaTask(area, new AreaTaskImpl<SearchResult>(false) {
            @Override
            protected SearchResult doMonitoredTask() {
                return getResultFromSolrDocument(oid, resultIndex, document);
            }
        });
    }
}
