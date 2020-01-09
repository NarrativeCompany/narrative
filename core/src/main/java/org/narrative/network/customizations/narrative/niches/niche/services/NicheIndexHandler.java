package org.narrative.network.customizations.narrative.niches.niche.services;

import org.narrative.common.persistence.OID;
import org.narrative.common.persistence.ObjectSeptuplet;
import org.narrative.common.persistence.ObjectTriplet;
import org.narrative.common.util.SubListIterator;
import org.narrative.common.util.processes.GenericProcess;
import org.narrative.network.core.area.user.AreaUser;
import org.narrative.network.core.cluster.partition.PartitionType;
import org.narrative.network.core.search.AreaRealmDataIndexHandlerBase;
import org.narrative.network.core.search.IndexType;
import org.narrative.network.core.search.SearchResult;
import org.narrative.network.core.user.AuthZone;
import org.narrative.network.customizations.narrative.niches.niche.Niche;
import org.narrative.network.customizations.narrative.niches.niche.NicheStatus;
import org.narrative.network.shared.tasktypes.AreaTaskImpl;
import org.narrative.network.shared.tasktypes.GlobalTaskImpl;
import org.narrative.network.shared.tasktypes.TaskRunner;
import org.narrative.network.shared.util.NetworkLogger;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;
import org.hibernate.ScrollableResults;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.narrative.common.util.CoreUtils.*;
import static org.narrative.network.shared.util.NetworkCoreUtils.*;

/**
 * Created by IntelliJ IDEA.
 * User: jonmark
 * Date: 2/13/18
 * Time: 8:12 AM
 */
public class NicheIndexHandler extends AreaRealmDataIndexHandlerBase {
    private static final NetworkLogger logger = new NetworkLogger(NicheIndexHandler.class);

    public static final String FIELD_NICHE_NAME_UNSTEMMED = "nicheNameUnstemmed";
    public static final String FIELD_NICHE_STATUS = "nicheStatus";
    public static final String FIELD_NICHE_OWNER = "nicheOwner";

    public NicheIndexHandler() {
        super(IndexType.NICHE);
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
                    List<ObjectSeptuplet<OID, OID, String, String, Timestamp, NicheStatus, OID>> datas = Niche.dao().getIndexRecordChunked(currentLastOid, 1000);
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

            process.updateStatusMessageAndLog(logger, "Finished writing indexes for " + rowsWritten + " niches.");

            if (dataCount < 1000) {
                break;
            }
        }

        process.updateStatusMessageAndLog(logger, "Finished writing indexes for niches. (" + rowsWritten + " records written)");
    }

    private Collection<SolrInputDocument> createDocumentChunk(List<ObjectSeptuplet<OID, OID, String, String, Timestamp, NicheStatus, OID>> datas, long indexVersion) {
        Collection<SolrInputDocument> documents = newLinkedList();

        Map<OID, OID> areaUserOidToUserOidLookup = AreaUser.dao().getAreaUserOidToUserOidMap(ObjectSeptuplet.getAllSevens(datas));

        for (ObjectSeptuplet<OID, OID, String, String, Timestamp, NicheStatus, OID> nicheData : datas) {
            OID nicheOid = nicheData.getOne();

            String name = nicheData.getThree();
            String description = nicheData.getFour();
            Timestamp suggestionDatetime = nicheData.getFive();
            OID areaOid = nicheData.getTwo();
            NicheStatus status = nicheData.getSix();
            OID ownerAreaUserOid = nicheData.getSeven();

            OID ownerUserOid = null;
            if (ownerAreaUserOid != null) {
                ownerUserOid = areaUserOidToUserOidLookup.get(ownerAreaUserOid);
            }

            SolrInputDocument document = createSolrInputDocument(nicheOid, name, description, suggestionDatetime, areaOid, status, ownerUserOid, indexVersion);

            if (document != null) {
                documents.add(document);
            }
        }
        PartitionType.GLOBAL.currentSession().clearSession();

        return documents;
    }

    @Override
    protected SolrInputDocument createSolrInputDocumentWithinArea(OID oid, long indexVersion, boolean forBulkOperation) {
        Niche niche = Niche.dao().get(oid);
        if (!exists(niche)) {
            logger.warn("Trying to index non-existent niche: " + oid);
            return null;
        }

        return createSolrInputDocument(oid, niche.getName(), niche.getDescription(), niche.getSuggestedDatetime(), niche.getArea().getOid(), niche.getStatus(), exists(niche.getOwner()) ? niche.getOwner().getUser().getOid() : null, indexVersion);
    }

    private SolrInputDocument createSolrInputDocument(OID nicheOid, String name, String description, Timestamp suggestionDatetime, OID areaOid, NicheStatus status, OID ownerUserOid, long indexVersion) {
        AuthZone authZone = AuthZone.getAuthZoneFromAreaOid(areaOid);

        StringBuilder fullText = new StringBuilder(name);
        if (!isEmpty(description)) {
            fullText.append(" " + description);
        }

        SolrInputDocument document = createDataDocument(nicheOid, null, name, fullText.toString(), suggestionDatetime, suggestionDatetime, authZone, areaOid, indexVersion);
        document.addField(FIELD_NICHE_NAME_UNSTEMMED, name);
        document.addField(FIELD_NICHE_STATUS, Integer.toString(status.getId()));
        if (ownerUserOid != null) {
            document.addField(FIELD_NICHE_OWNER, ownerUserOid.getValue());
        }

        return document;
    }

    @Override
    protected SearchResult getResultFromSolrDocument(OID oid, int resultIndex, SolrDocument document) {
        Timestamp liveDatetime = new Timestamp(((Date) document.get(FIELD__COMMON__ITEM_DATE)).getTime());

        return new NicheSearchResult(oid, resultIndex, liveDatetime);
    }

    protected ScrollableResults getAllIdsScrollable() {
        return Niche.dao().getAllOidsAndAreaOids();
    }
}
