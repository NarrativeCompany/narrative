package org.narrative.network.core.search;

import org.narrative.common.persistence.OID;
import org.narrative.common.util.ApplicationError;
import org.narrative.common.util.IPDateUtil;
import org.narrative.common.util.IPStringUtil;
import org.narrative.common.util.IPUtil;
import org.narrative.common.util.Stemmer;
import org.narrative.common.util.UnexpectedError;
import org.narrative.network.core.cluster.partition.PartitionGroup;
import org.narrative.network.core.search.services.IndexOperationJob;
import org.narrative.network.core.search.services.OidSearchUtil;
import org.narrative.network.core.search.services.SearchCriteria;
import org.narrative.network.core.search.services.SearchResults;
import org.narrative.network.core.search.services.SolrClientTask;
import org.narrative.network.core.settings.global.services.GlobalSettingsUtil;
import org.narrative.network.core.statistics.StatisticManager;
import org.narrative.network.core.user.AuthZone;
import org.narrative.network.core.rating.AgeRating;
import org.narrative.network.shared.tasktypes.GlobalTaskImpl;
import org.narrative.network.shared.tasktypes.TaskRunner;
import org.narrative.network.shared.util.NetworkLogger;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CloudSolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.request.UpdateRequest;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.SolrInputDocument;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;

import static org.narrative.common.util.CoreUtils.*;
import static org.narrative.network.shared.util.NetworkCoreUtils.*;

/**
 * Date: May 9, 2008
 * Time: 1:19:38 PM
 *
 * @author Paul Malolepsy
 */
public abstract class IndexHandler implements Runnable {
    private static final NetworkLogger logger = new NetworkLogger(IndexHandler.class);

    public static final String FIELD__COMMON__INDEX_TYPE = "indexType";
    public static final String FIELD__COMMON__INDEX_VERSION = "indexVersion";
    public static final String FIELD__COMMON__ID = "id";
    public static final String FIELD__COMMON__NAME = "name";
    public static final String FIELD__COMMON__FULL_TEXT = "fullText";
    public static final String FIELD__COMMON__ITEM_DATE = "itemDate";
    public static final String FIELD__COMMON__AUTH_ZONE = "authZone";
    public static final String FIELD__COMMON__ALLOW_REPLIES = "allowReplies";
    public static final String FIELD__COMMON__AGE_RATING = "ageRating";

    private final IndexType indexType;

    private final LinkedBlockingQueue<IndexOperation> indexWriterQueue = new LinkedBlockingQueue<>();

    private volatile boolean shouldStop = true;

    private static String solrServerUrl;
    private static List<String> solrZookeeperHosts;
    private static String solrCloudDefaultCollection;
    private static Timestamp testSolrServerAt = null;

    // jw: it is HIGHLY recommended that we use a single static SolrServer for our type of use case.  Creating the
    //     SolrServer when you need to send a request to the server can cause a connection leak.  The CommonsHttpSolrServer
    //     is thread safe (according to documentation).
    private static SolrClient solrClient;

    public static void init(String solrServerUrl, List<String> solrZookeeperHosts, String solrCloudDefaultCollection) {
        IndexHandler.solrServerUrl = solrServerUrl;
        IndexHandler.solrZookeeperHosts = solrZookeeperHosts;
        IndexHandler.solrCloudDefaultCollection = solrCloudDefaultCollection;
    }

    private static final long MS_BETWEEN_SOLR_SERVER_CHECKS = 5L * IPDateUtil.SECOND_IN_MS;

    private static final int SERVER_CONNECTION_TIMEOUT = 30 * IPDateUtil.SECOND_IN_MS;

    // Whenever we use a SolrServer lets make sure its been initialized.
    private static SolrClient getSolrClient() {
        if (solrClient != null) {
            return solrClient;
        }

        if (testSolrServerAt == null || testSolrServerAt.before(new Timestamp(System.currentTimeMillis()))) {
            synchronized (IndexHandler.class) {
                if (solrClient == null) {
                    solrClient = createSolrClient(solrServerUrl, solrZookeeperHosts, solrCloudDefaultCollection);

                    testSolrClient();
                }
            }
        }
        return solrClient;
    }

    private static SolrClient createSolrClient(String solrServerUrl, List<String> solrZookeeperHosts, String solrCloudDefaultCollection) {
        // jw: if we have a solrServerUrl, then we are connecting directly to a single solr instance, so lets do that.
        if (!StringUtils.isEmpty(solrServerUrl)) {
            return new HttpSolrClient.Builder(solrServerUrl).withConnectionTimeout(SERVER_CONNECTION_TIMEOUT).build();
        }

        if (isEmptyOrNull(solrZookeeperHosts)) {
            throw UnexpectedError.getRuntimeException("Must specify at least one solrZookeeperHosts if not specifying the solrServerUrl.");
        }

        if (StringUtils.isEmpty(solrCloudDefaultCollection)) {
            throw UnexpectedError.getRuntimeException("Must specify the solrCloudDefaultCollection when running in a cloud configuration (solrZookeeperHosts).");
        }

        CloudSolrClient client = new CloudSolrClient.Builder(solrZookeeperHosts, Optional.empty()).withConnectionTimeout(SERVER_CONNECTION_TIMEOUT).build();
        client.setDefaultCollection(solrCloudDefaultCollection);

        return client;
    }

    private static void testSolrClient() {
        Exception exception = null;
        try {
            SolrQuery query = new SolrQuery("*:*");
            query.setStart(0).setRows(1);

            QueryResponse response = solrClient.query(query);

            // if we did not get a OK status then lets go ahead and report that as a error down below
            if (response.getStatus() != 0) {
                exception = UnexpectedError.getRuntimeException("Got unexpected status from SolrServer test query: " + response.getStatus());
            }

        } catch (SolrServerException e) {
            exception = e;

        } catch (IOException e) {
            throw UnexpectedError.getRuntimeException("Issue communicating with Solr Server! " + getConfigLogDetails(), e);
        }

        if (exception != null) {
            markSolrServerAsDown();
            if (logger.isErrorEnabled()) {
                logger.error("Failed testing SolrServer: " + getConfigLogDetails() + ", should ensure server is running and ready for connections!", exception);
            }
            StatisticManager.recordException(exception, false, null);
        } else {
            testSolrServerAt = null;
        }
    }

    private static String getConfigLogDetails() {
        if (!StringUtils.isEmpty(solrServerUrl)) {
            return "solrServerUrl: " + solrServerUrl;
        }

        return "solrZookeeperHosts: " + IPStringUtil.getCommaSeparatedList(solrZookeeperHosts);
    }

    private static void markSolrServerAsDown() {
        solrClient = null;
        testSolrServerAt = new Timestamp(System.currentTimeMillis() + MS_BETWEEN_SOLR_SERVER_CHECKS);
    }

    private static boolean isSolrServerDown() {
        // if we currently cannot establish a connection to the solr server then I guess its down.
        return getSolrClient() == null;
    }

    protected IndexHandler(IndexType indexType) {
        this.indexType = indexType;
    }

    public abstract void auditAndFixMissingItems();

    protected abstract SearchResult getResultFromSolrDocument(SolrDocument document, OID oid, int resultIndex);

    public abstract void rebuildSolrIndex(long indexVersion);

    protected abstract SolrInputDocument createSolrInputDocument(final IndexOperationId id, long indexVersion, boolean forBulkOperation);

    //bk: is this the best place?
    public void bulkFetchResults(List<? extends SearchResult> results) {
        //by default does nothing
    }

    public final void performOperation(final IndexOperation operation) {
        // If we are currently in a transaction, perform the index operation after the transaction is committed so we
        // don't have a race between re-indexing and the transaction commit.  This method queues an operation that will
        // be executed outside of the calling thread.
        if(PartitionGroup.isCurrentPartitionGroupSet() && operation.getOperation()== IndexOperation.Type.UPDATE) {
            PartitionGroup.getCurrentPartitionGroup().addEndOfGroupRunnable(() -> {
                indexWriterQueue.add(operation);
            });
        } else {
            indexWriterQueue.add(operation);
        }
    }

    public final IndexType getIndexType() {
        return indexType;
    }

    public synchronized void start() {
        shouldStop = false;

        Thread t = new Thread(this, "IndexManager-" + getIndexType().name());
        t.setDaemon(true);
        t.start();

        if (logger.isInfoEnabled()) {
            logger.info(indexType + " IndexHandler started.");
        }
    }

    public final void stop() {
        shouldStop = true;
    }

    private static final int MAX_DOCUMENTS_TO_WRITE_PER_GROUP = 100;

    @Override
    public void run() {
        Map<SolrInputDocument, IndexOperation> documentsToWrite = newLinkedHashMap();
        try {
            do {
                final IndexOperation indexOperation = indexWriterQueue.take();
                try {
                    if (indexOperation == null || indexOperation.operation == IndexOperation.Type.NOOP) {
                        continue;
                    }

                    // if we have lost connection to the server, lets check to see what we should do
                    if (isSolrServerDown()) {
                        // if we should stop lets add durable jobs for all operations that are left in the queue.
                        if (shouldStop) {
                            if (logger.isWarnEnabled()) {
                                logger.warn("Failed connecting to SolrServer during IndexHandler run cycle after shutdown, flushing " + (indexWriterQueue.size() + 1) + " operations to Job!");
                            }
                            IndexOperationJob.schedule(getIndexType(), indexOperation);
                            while (!indexWriterQueue.isEmpty()) {
                                IndexOperation operation = indexWriterQueue.take();

                                IndexOperationJob.schedule(getIndexType(), operation);
                            }

                            // if we are not shutting down lets try again in 5 seconds
                        } else {
                            logger.warn("Failed connecting to SolrServer during IndexHandler run cycle, waiting 5 seconds!");
                            // add the op back into the queue so we'll process it again later.
                            indexWriterQueue.add(indexOperation);
                            IPUtil.uninterruptedSleep(5 * IPDateUtil.SECOND_IN_MS);
                        }

                        // continue on, if shutting down this should cause the thread to run out.
                        continue;
                    }

                    if (logger.isDebugEnabled()) {
                        logger.debug("Performing " + indexOperation.operation + " on index: " + indexType);
                    }

                    switch (indexOperation.operation) {
                        case UPDATE:

                            SolrInputDocument document = TaskRunner.doRootGlobalTask(new GlobalTaskImpl<SolrInputDocument>(false) {
                                @Override
                                protected SolrInputDocument doMonitoredTask() {
                                    return createSolrInputDocument(indexOperation.id, GlobalSettingsUtil.getGlobalSettings().getSolrIndexVersion(), false);
                                }
                            });

                            // bl: the document may be null if the item being indexed doesn't exist anymore.
                            if (document != null) {
                                documentsToWrite.put(document, indexOperation);
                            }

                            boolean success;
                            if (indexWriterQueue.isEmpty()) {
                                // if the queue is empty lets flush the current documents out to the server.
                                success = addDocuments(documentsToWrite.keySet());
                            } else {
                                success = addDocumentChunk(documentsToWrite.keySet());
                            }
                            if (!success) {
                                // if adding the documents failed
                                indexWriterQueue.addAll(documentsToWrite.values());
                                documentsToWrite.clear();
                            }
                            break;
                        case DELETE:
                            if (!deleteById(indexOperation.id.getDocId().toString())) {
                                indexWriterQueue.add(indexOperation);
                            }
                            break;
                        default:
                            break;
                    }
                } catch (Throwable t) {
                    IndexOperationId opId = indexOperation.id;
                    logger.error("Failed index operation.  Continuing on. index/" + getIndexType() + " op/" + indexOperation.operation + (opId != null ? " op/" + opId.getDocId() + "/" + opId.getExtraDataOid() : ""), t);
                    StatisticManager.recordException(t, false, null);
                }
            } while (!shouldStop);
        } catch (Exception e) {
            throw UnexpectedError.getRuntimeException("IndexManager interrupted for index: " + getIndexType(), e);
        } finally {
            try {
                if (!addDocuments(documentsToWrite.keySet())) {
                    throw UnexpectedError.getRuntimeException("Failed adding remaining documents at IndexHandler shutdown!");
                }
            } catch (Exception e) {
                logger.error("IndexManager could not flush remaining documents: " + getIndexType(), e);
                StatisticManager.recordException(e, false, null);
            }
        }
    }

    void addDocumentChunkSafely(Collection<SolrInputDocument> documents, OID lastOid) {
        try {
            addDocumentChunk(documents);
        } catch (Throwable t) {
            logger.error("Failed indexing " + documents.size() + " documents -- after last oid/" + lastOid + " for indexType/" + getIndexType() + ". Ignoring and continuing on.", t);
            StatisticManager.recordException(t, false, null);
            // clear out the documents, since one of them may be a "problem child"
            documents.clear();
        }
    }

    private static boolean addDocumentChunk(Collection<SolrInputDocument> documents) throws SolrServerException {
        if (documents.size() < MAX_DOCUMENTS_TO_WRITE_PER_GROUP) {
            return true;
        }
        return addDocuments(documents);
    }

    protected void addDocumentsSafely(Collection<SolrInputDocument> documents) {
        try {
            addDocuments(documents);
        } catch (Throwable t) {
            logger.error("Failed indexing " + documents.size() + " documents on OID chunk completion. Ignoring and continuing on.", t);
            StatisticManager.recordException(t, false, null);
        }
    }

    private static boolean addDocuments(final Collection<SolrInputDocument> documents) throws SolrServerException {
        if (documents.isEmpty()) {
            return true;
        }
        try {
            Boolean ret = runSolrClientTask(client -> {
                UpdateRequest request = new UpdateRequest();
                // jw: We are now going to use commit within 5 seconds so that solr can have tighter control over commits
                //     which should alleviate the load on the warming threads.
                request.setCommitWithin(5 * IPDateUtil.SECOND_IN_MS);
                request.add(documents);
                request.process(client);
                documents.clear();

                return Boolean.TRUE;
            });
            return ret != null && ret;
        } catch (SolrServerException | SolrException e) {
            String error = "Failed adding " + documents.size() + " documents";
            logger.error(error, e);
            StatisticManager.recordException(UnexpectedError.getRuntimeException(error, e), false, null);
            throw e;
        }
    }

    private static boolean deleteById(final String id) throws SolrServerException {
        Boolean ret = runSolrClientTask(client -> {
            client.deleteById(id);

            return Boolean.TRUE;
        });
        return ret != null && ret;
    }

    private static void deleteByQuery(final Query query) throws SolrServerException {
        try {
            runSolrClientTask(client -> {
                client.deleteByQuery(query.toString());

                return Boolean.TRUE;
            });
        } catch (SolrServerException | SolrException e) {
            String error = "Failed deleting by query/" + query;
            logger.error(error, e);
            StatisticManager.recordException(UnexpectedError.getRuntimeException(error, e), false, null);
            throw e;
        }
    }

    private static QueryResponse query(final SolrQuery query) {
        QueryResponse response = null;
        try {
            // bl: in order to allow longer queries, let's always use HTTP POST now!
            response = runSolrClientTask(client -> client.query(query, SolrRequest.METHOD.POST));

        } catch (SolrServerException | SolrException e) {
            String error = "Failed executing Solr query/" + query;
            logger.error(error, e);
            StatisticManager.recordException(UnexpectedError.getRuntimeException(error, e), false, null);
        }
        if (response == null) {
            throw new ApplicationError(wordlet("error.searchCurrentlyUnavailable"));
        }
        return response;
    }

    public static void optimizeSolrServer() {
        try {
            runSolrClientTask(client -> {
                client.optimize();

                return Boolean.TRUE;
            });
        } catch (SolrServerException | SolrException e) {
            String error = "Failed optimizing solr server index.";
            logger.error(error, e);
            StatisticManager.recordException(UnexpectedError.getRuntimeException(error, e), false, null);
        }
    }

    private static <R> R runSolrClientTask(SolrClientTask<R> task) throws SolrServerException {
        // jw: lets ensure that if any errors happen while taking action on the SolrClient that the cached solrClient
        //     will be marked as invalid and thus cause the app to wait a bit before allowing the client to try and
        //     do anything else.
        SolrClient client = getSolrClient();
        if (client != null) {
            try {
                return task.doTask(client);

            // jw: CloudSolrClient is throwing a RuntimeException when it fails to negotiate with the cluster. We need
            //     to ensure that we are catching all exceptions, and flagging that the server is down properly.
            } catch (Exception e) {
                if (logger.isErrorEnabled()) logger.error("Failed executing SolrServerTask for Exception!", e);
                StatisticManager.recordException(e, false, null);
            }
            markSolrServerAsDown();
        }

        return null;
    }

    Set<OID> getIdsNotInIndex(Collection<OID> idsToCheck) {
        // first, lets build a query to fetch all search results for the provided ids
        BooleanQuery query = new BooleanQuery();
        query.add(OidSearchUtil.addOidsToBooleanQuery(idsToCheck, new BooleanQuery(), FIELD__COMMON__ID, BooleanClause.Occur.SHOULD), BooleanClause.Occur.MUST);

        // Now lets run the search and filter out any results that actually came back
        Set<OID> oidsNotInIndex = newHashSet(idsToCheck);
        SearchResults results = search(query, false, idsToCheck.size(), 0, true);
        for (SearchResult result : results.getResults()) {
            oidsNotInIndex.remove(result.getOid());
        }

        return oidsNotInIndex;
    }

    public SearchResults search(BooleanQuery query, boolean isDateSort, int resultCount, Integer start, boolean justIds) {
        return search(EnumSet.of(getIndexType()), query, isDateSort, resultCount, start, justIds);
    }

    public static SearchResults search(Collection<IndexType> indexTypes, BooleanQuery query, boolean isDateSort, int resultCount, Integer start, boolean justIds) {
        resultCount = Math.max(1, Math.min(resultCount, 5000));

        BooleanQuery indexTypesQuery = new BooleanQuery();
        for (IndexType indexType : indexTypes) {
            indexTypesQuery.add(indexType.getTermQuery(), BooleanClause.Occur.SHOULD);
        }
        indexTypesQuery.setBoost(0);
        query.add(indexTypesQuery, BooleanClause.Occur.MUST);

        SolrQuery solrQuery = new SolrQuery(SearchCriteria.escapeSolrQueryString(query.toString()));
        if (isDateSort) {
            solrQuery.setSort(FIELD__COMMON__ITEM_DATE, SolrQuery.ORDER.desc);
        }
        solrQuery.setRows(resultCount);

        int documentIndex = start != null ? start : 0;

        List<SearchResult> results = newLinkedList();
        Map<IndexType, List<SearchResult>> groupedResults = newHashMap();
        solrQuery.setStart(documentIndex);

        QueryResponse response = query(solrQuery);

        long totalResults = response.getResults().getNumFound();

        for (SolrDocument document : response.getResults()) {
            OID oid = OID.valueOf(document.get(FIELD__COMMON__ID));
            IndexType indexType = IndexType.getFromCode((String) document.get(FIELD__COMMON__INDEX_TYPE));
            documentIndex++;

            if (indexType == null) {
                continue;
            }

            SearchResult result = indexType.getIndexHandler().getResultFromSolrDocument(document, oid, documentIndex);

            results.add(result);
            addMapListLookupValue(groupedResults, indexType, result);
        }

        // allow the index handler to bulk fetch the results
        if (!justIds) {
            for (Map.Entry<IndexType, List<SearchResult>> groupingEntry : groupedResults.entrySet()) {
                groupingEntry.getKey().getIndexHandler().bulkFetchResults(groupingEntry.getValue());
            }
        }

        return new SearchResults(results, totalResults);
    }

    public void rebuildIndex() {
        final long indexVersion = TaskRunner.doRootGlobalTask(new GlobalTaskImpl<Long>() {
            @Override
            protected Long doMonitoredTask() {
                return GlobalSettingsUtil.getGlobalSettingsForWrite().incrementSolrIndexVersion();
            }
        });
        TaskRunner.doRootGlobalTask(new GlobalTaskImpl<Object>(false) {
            @Override
            protected Object doMonitoredTask() {
                rebuildSolrIndex(indexVersion);

                try {
                    // now that this index is up to date lets clear all stale values that are no longer in the database by removing
                    // any documents for this type
                    deleteByQuery(getBaseDeleteIndexQuery(indexVersion));

                } catch (SolrServerException e) {
                    logger.error("Failed rebuilding index due to SolrServerException.", e);
                    StatisticManager.recordException(e, false, null);
                }
                return null;
            }
        });
    }

    private BooleanQuery getBaseDeleteIndexQuery(long rebuildVersion) {
        long currentVersion = TaskRunner.doRootGlobalTask(new GlobalTaskImpl<Long>(false) {
            @Override
            protected Long doMonitoredTask() {
                return GlobalSettingsUtil.getGlobalSettings().getSolrIndexVersion();
            }
        });
        BooleanQuery query = new BooleanQuery();
        query.add(getIndexType().getTermQuery(), BooleanClause.Occur.MUST);
        BooleanQuery versionQuery = new BooleanQuery();
        for (long i = rebuildVersion; i <= currentVersion; i++) {
            versionQuery.add(new TermQuery(new Term(AreaDataIndexHandlerBase.FIELD__COMMON__INDEX_VERSION, Long.toString(i))), BooleanClause.Occur.SHOULD);
        }
        query.add(versionQuery, BooleanClause.Occur.MUST_NOT);
        return query;
    }

    private void addSharedOptionalField(SolrInputDocument document, String name, String value) {
        if (isEmpty(value)) {
            return;
        }
        document.addField(name, value);
    }

    SolrInputDocument createDefaultDocument(OID oid, String name, String fullText, Timestamp itemDate, AuthZone authZone, long indexVersion) {
        return createDefaultDocument(oid, name, fullText, itemDate, authZone, null, indexVersion);
    }

    SolrInputDocument createDefaultDocument(OID oid, String name, String fullText, Timestamp itemDate, AuthZone authZone, AgeRating ageRating, long indexVersion) {
        SolrInputDocument document = new SolrInputDocument();
        document.addField(FIELD__COMMON__INDEX_TYPE, indexType.getIdStr());
        document.addField(FIELD__COMMON__INDEX_VERSION, Long.toString(indexVersion));
        document.addField(FIELD__COMMON__ID, oid.toString());
        document.addField(FIELD__COMMON__ITEM_DATE, new Date(itemDate.getTime()));
        document.addField(FIELD__COMMON__AUTH_ZONE, authZone.getOid().getValue());
        document.addField(FIELD__COMMON__AGE_RATING, ageRating != null ? ageRating.getId() : AgeRating.GENERAL.getId());

        addSharedOptionalField(document, FIELD__COMMON__NAME, Stemmer.getStemmedText(name));
        addSharedOptionalField(document, FIELD__COMMON__FULL_TEXT, Stemmer.getStemmedText(fullText));

        return document;
    }

}
