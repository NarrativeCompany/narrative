package org.narrative.network.core.search;

import org.narrative.common.persistence.OID;
import org.narrative.common.util.SubListIterator;
import org.narrative.common.util.processes.ProcessManager;
import org.narrative.network.shared.daobase.NetworkDAOImpl;
import org.narrative.network.shared.tasktypes.GlobalTaskImpl;
import org.narrative.network.shared.tasktypes.TaskRunner;
import org.narrative.network.shared.util.NetworkLogger;
import org.apache.solr.common.SolrDocument;
import org.hibernate.ScrollableResults;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import static org.narrative.common.util.CoreUtils.*;

/**
 * User: barry
 * Date: Sep 11, 2009
 * Time: 10:33:35 AM
 */
public abstract class SimpleIndexHandler extends IndexHandler {

    private static final NetworkLogger logger = new NetworkLogger(SimpleIndexHandler.class);

    protected SimpleIndexHandler(IndexType indexType) {
        super(indexType);
    }

    protected abstract SearchResult getResultFromDocument(SolrDocument document, OID oid, int resultIndex, String name);

    @Override
    protected SearchResult getResultFromSolrDocument(SolrDocument document, OID oid, int resultIndex) {
        String name = (String) document.get(FIELD__COMMON__NAME);
        return getResultFromDocument(document, oid, resultIndex, name);
    }

    public void auditAndFixMissingItems() {
        // this should do an audit and simply add items to the indexWriterBlockingQueue
        // items added to the queue (updates,deletes,add,optimize) will run and the
        // search index reader will be reloaded once it is done

        ProcessManager.getInstance().getCurrentProcess().updateStatusMessageAndLog(logger, "Starting index audit for missing items of type " + getIndexType());

        List<OID> missingOids = getMissingOids();
        ProcessManager.getInstance().getCurrentProcess().updateStatusMessageAndLog(logger, "Found " + missingOids.size() + " missing items for " + getIndexType() + " index.");
        SubListIterator<OID> iter = new SubListIterator<OID>(missingOids, 10);

        while (iter.hasNext()) {
            List<OID> oidsToDo = iter.next();
            for (OID oid : oidsToDo) {
                performOperation(IndexOperation.update(oid));
            }
        }

        ProcessManager.getInstance().getCurrentProcess().updateStatusMessageAndLog(logger, "Finished index audit for " + getIndexType() + ". Added " + missingOids.size() + " missing items to " + getIndexType() + " index.");
    }

    protected abstract ScrollableResults getAllIdsScrollable();

    protected abstract NetworkDAOImpl getNetworkDAO();

    private List<OID> getMissingOids() {
        final List<OID> oidsToAdd = new LinkedList<OID>();

        TaskRunner.doRootGlobalTask(new GlobalTaskImpl(false) {
            protected Object doMonitoredTask() {
                ScrollableResults results = getAllIdsScrollable();

                int rowsAudited = 0;

                Collection<OID> oids = newHashSet();
                while (results.next()) {
                    rowsAudited++;
                    oids.add((OID) results.get(0));

                    if (rowsAudited % 500 == 0) {
                        for (OID oid : getIdsNotInIndex(oids)) {
                            oidsToAdd.add(oid);
                        }
                        oids.clear();
                        if ((rowsAudited % 5000) == 0) {
                            ProcessManager.getInstance().getCurrentProcess().updateStatusMessageAndLog(logger, "Audit index missing items for " + rowsAudited + " " + getIndexType() + " items.");
                        }
                    }
                }
                if (!oids.isEmpty()) {
                    for (OID oid : getIdsNotInIndex(oids)) {
                        oidsToAdd.add(oid);
                    }
                }

                return null;
            }
        });

        return oidsToAdd;
    }

    public void bulkFetchResults(List<? extends SearchResult> results) {
        Set<OID> oids = new HashSet<OID>();
        for (SearchResult result : results) {
            oids.add(result.getOid());
        }
        getNetworkDAO().getObjectsFromIDsWithCache(oids);
    }
}
