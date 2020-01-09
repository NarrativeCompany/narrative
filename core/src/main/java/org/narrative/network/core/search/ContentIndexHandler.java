package org.narrative.network.core.search;

import org.narrative.common.persistence.OID;
import org.narrative.common.persistence.ObjectPair;
import org.narrative.common.util.UnexpectedError;
import org.narrative.common.util.html.HTMLStripper;
import org.narrative.common.util.processes.GenericProcess;
import org.narrative.network.core.composition.base.Composition;
import org.narrative.network.core.content.base.Content;
import org.narrative.network.customizations.narrative.channels.Channel;
import org.narrative.network.customizations.narrative.posts.ChannelContent;
import org.narrative.network.shared.tasktypes.CompositionTaskImpl;
import org.narrative.network.shared.util.NetworkLogger;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;
import org.hibernate.ScrollableResults;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static org.narrative.common.util.CoreUtils.*;
import static org.narrative.network.shared.util.NetworkCoreUtils.*;

/**
 * User: barry
 * Date: Sep 13, 2009
 * Time: 11:30:05 PM
 */
public class ContentIndexHandler extends AreaRealmDataIndexHandlerBase implements CompositionIndexFields {
    private static final NetworkLogger logger = new NetworkLogger(ContentIndexHandler.class);

    public static final String FIELD_CONTENT_TYPE = "contentType";

    public ContentIndexHandler() {
        super(IndexType.CONTENT);
    }

    @Override
    protected void rebuildIndex(GenericProcess process, long indexVersion) {
        final List<ObjectPair<OID, OID>> contentAndAreaOids = Content.dao().getAllOids();
        buildIndexForOids(process, contentAndAreaOids, indexVersion);
    }

    private void buildIndexForOids(GenericProcess process, List<ObjectPair<OID, OID>> contentAndAreaOids, long indexVersion) {
        process.updateStatusMessageAndLog(logger, "Starting content index rebuild for " + contentAndAreaOids.size() + " posts.");
        int i = 0;
        Collection<SolrInputDocument> documents = newLinkedList();
        for (final ObjectPair<OID, OID> pair : contentAndAreaOids) {
            OID contentOid = pair.getOne();
            OID areaOid = pair.getTwo();
            i++;
            SolrInputDocument document = createSolrInputDocument(new IndexOperationId(contentOid, areaOid), indexVersion, true);
            if (document != null) {
                documents.add(document);
                addDocumentChunkSafely(documents, contentOid);
            }
            if ((i % 500) == 0) {
                process.updateStatusMessageAndLog(logger, "Completed content index rebuild for " + i + " of " + contentAndAreaOids.size() + " posts.");
            }
        }

        addDocumentsSafely(documents);

        process.updateStatusMessageAndLog(logger, "Finished content index rebuild. " + contentAndAreaOids.size() + " posts indexed.");
    }

    @Override
    public void auditAndFixMissingItems() {
        logger.warn("auditAndFixMissingItems of content indexes not yet implemented");
    }

    @Override
    protected ScrollableResults getAllIdsScrollable() {
        throw UnexpectedError.getRuntimeException("Don't support index audit for content currently!");
    }

    @Override
    protected SolrInputDocument createSolrInputDocumentWithinArea(OID oid, final long indexVersion, final boolean forBulkOperation) {
        final Content content = Content.dao().get(oid);
        if (!exists(content)) {
            logger.warn("Trying to index non-existent content: " + oid);
            return null;
        }

        return networkContext().doCompositionTask(content.getCompositionPartition(), new CompositionTaskImpl<SolrInputDocument>(false) {
            protected SolrInputDocument doMonitoredTask() {
                Composition composition = content.getComposition();
                String fullText = content.getFullText(true) + ' ' + composition.getFullText(true);
                // bl: no point in indexing html entities, so set stripEntities to true
                fullText = HTMLStripper.stripHtmlFragment(fullText, true);

                SolrInputDocument document = createDataDocument(
                        content.getOid(),
                        content.isAuthorAllowed() && exists(content.getRealAuthor()) ? content.getRealAuthor().getOid() : null,
                        content.getSubject(),
                        fullText,
                        content.getItemDatetimeForSearchIndex(),
                        content.getLastUpdateDatetime(),
                        content.getAuthZone(),
                        content.getArea().getOid(),
                        content.getAgeRating(),
                        indexVersion
                );

                addContentDocumentFields(document, content, false);

                document.addField(FIELD_HAS_ATTACHMENTS, composition.hasAttachments());

                return document;
            }
        });
    }

    public static void addContentDocumentFields(final SolrInputDocument document, final Content content, final boolean isForReply) {
        document.addField(FIELD_CONTENT_TYPE, Integer.toString(content.getContentType().getId()));

        if (!isForReply) {
            document.addField(FIELD__COMMON__ALLOW_REPLIES, content.isAllowReplies());
        }

        // jw: let's add the narrative post specific fields!
        if (content.getContentType().isNarrativePost()) {
            // bl: only index channels where the content is approved
            List<Channel> channels = content.getChannelContentsInited().values().stream()
                    .filter(cc -> cc.getStatus().isApproved())
                    .map(ChannelContent::getChannel)
                    .collect(Collectors.toList());
            for (Channel channel : channels) {
                document.addField(FIELD_CHANNEL_OID, channel.getOid().getValue());
            }
        }
    }

    @Override
    protected SearchResult getResultFromSolrDocument(OID oid, int resultIndex, SolrDocument document) {
        throw UnexpectedError.getRuntimeException("Not currently supporting getResultFromSolrDocument for the content index.");
    }

    @Override
    protected SearchResult getResultFromSolrDocument(SolrDocument document, OID oid, int resultIndex) {
        Timestamp liveDatetime = new Timestamp(((Date) document.get(FIELD__COMMON__ITEM_DATE)).getTime());

        return new ContentSearchResult(oid, resultIndex, liveDatetime);
    }
}
