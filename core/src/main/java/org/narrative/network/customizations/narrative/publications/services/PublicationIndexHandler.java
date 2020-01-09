package org.narrative.network.customizations.narrative.publications.services;

import org.narrative.common.persistence.OID;
import org.narrative.common.persistence.ObjectQuadruplet;
import org.narrative.common.persistence.ObjectTriplet;
import org.narrative.common.util.processes.GenericProcess;
import org.narrative.network.core.area.base.Area;
import org.narrative.network.core.cluster.partition.PartitionType;
import org.narrative.network.core.search.AreaRealmDataIndexHandlerBase;
import org.narrative.network.core.search.IndexType;
import org.narrative.network.core.search.SearchResult;
import org.narrative.network.core.user.AuthZone;
import org.narrative.network.customizations.narrative.publications.Publication;
import org.narrative.network.shared.tasktypes.GlobalTaskImpl;
import org.narrative.network.shared.tasktypes.TaskRunner;
import org.narrative.network.shared.util.NetworkLogger;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;
import org.hibernate.ScrollableResults;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Date: 2019-08-07
 * Time: 09:43
 *
 * @author jonmark
 */
public class PublicationIndexHandler extends AreaRealmDataIndexHandlerBase {
    private static final NetworkLogger logger = new NetworkLogger(PublicationIndexHandler.class);

    public static final String FIELD_PUBLICATION_NAME_UNSTEMMED = "publicationNameUnstemmed";

    public PublicationIndexHandler() {
        super(IndexType.PUBLICATION);
    }

    @Override
    protected void rebuildIndex(final GenericProcess process, final long indexVersion) {
        OID lastOid = new OID(0);
        int rowsWritten = 0;
        while (true) {
            final OID currentLastOid = lastOid;
            ObjectTriplet<Integer, OID, Integer> dataCountLastOidAndRowsWritten = TaskRunner.doRootGlobalTask(new GlobalTaskImpl<ObjectTriplet<Integer, OID, Integer>>(false) {
                @Override
                protected ObjectTriplet<Integer, OID, Integer> doMonitoredTask() {
                    List<ObjectQuadruplet<OID, String, String, Instant>> datas = Publication.dao().getIndexRecordChunked(currentLastOid, 1000);
                    if (isEmptyOrNull(datas)) {
                        return new ObjectTriplet<>(0, null, 0);
                    }

                    Collection<SolrInputDocument> documents = createDocumentChunk(datas, indexVersion);

                    // bl: the documents collection gets cleared when we addDocumentsSafely, so get the count ahead of time.
                    int docCount = documents.size();

                    addDocumentsSafely(documents);

                    return new ObjectTriplet<>(datas.size(), datas.get(datas.size() - 1).getOne(), docCount);
                }
            });
            int dataCount = dataCountLastOidAndRowsWritten.getOne();
            lastOid = dataCountLastOidAndRowsWritten.getTwo();
            rowsWritten += dataCountLastOidAndRowsWritten.getThree();

            process.updateStatusMessageAndLog(logger, "Finished writing indexes for " + rowsWritten + " publications.");

            if (dataCount < 1000) {
                break;
            }
        }

        process.updateStatusMessageAndLog(logger, "Finished writing indexes for publications. (" + rowsWritten + " records written)");
    }

    private Collection<SolrInputDocument> createDocumentChunk(List<ObjectQuadruplet<OID, String, String, Instant>> datas, long indexVersion) {
        Collection<SolrInputDocument> documents = newLinkedList();

        for (ObjectQuadruplet<OID, String, String, Instant> data : datas) {
            OID publicationOid = data.getOne();

            String name = data.getTwo();
            String description = data.getThree();
            Instant creationDatetime = data.getFour();

            SolrInputDocument document = createSolrInputDocument(publicationOid, name, description, creationDatetime, indexVersion);

            if (document != null) {
                documents.add(document);
            }
        }
        PartitionType.GLOBAL.currentSession().clearSession();

        return documents;
    }

    @Override
    protected SolrInputDocument createSolrInputDocumentWithinArea(OID oid, long indexVersion, boolean forBulkOperation) {
        Publication publication = Publication.dao().get(oid);
        if (!exists(publication)) {
            logger.warn("Trying to index non-existent publication: " + oid);
            return null;
        }

        return createSolrInputDocument(oid, publication.getName(), publication.getDescription(), publication.getCreationDatetime(), indexVersion);
    }

    private SolrInputDocument createSolrInputDocument(OID publicationOid, String name, String description, Instant creationDatetime, long indexVersion) {
        OID areaOid = Area.dao().getNarrativePlatformArea().getOid();
        AuthZone authZone = AuthZone.getAuthZoneFromAreaOid(areaOid);

        StringBuilder fullText = new StringBuilder(name);
        if (!isEmpty(description)) {
            fullText.append(" ").append(description);
        }

        Timestamp datetime = new Timestamp(creationDatetime.toEpochMilli());

        SolrInputDocument document = createDataDocument(publicationOid, null, name, fullText.toString(), datetime, datetime, authZone, areaOid, indexVersion);
        document.addField(FIELD_PUBLICATION_NAME_UNSTEMMED, name);

        return document;
    }

    @Override
    protected SearchResult getResultFromSolrDocument(OID oid, int resultIndex, SolrDocument document) {
        Timestamp liveDatetime = new Timestamp(((Date) document.get(FIELD__COMMON__ITEM_DATE)).getTime());

        return new PublicationSearchResult(oid, resultIndex, liveDatetime);
    }

    protected ScrollableResults getAllIdsScrollable() {
        return Publication.dao().getAllOidsAndAreaOids();
    }
}
