package org.narrative.network.core.search;

import org.narrative.common.persistence.OID;
import org.narrative.common.persistence.ObjectPair;
import org.narrative.common.util.SubListIterator;
import org.narrative.common.util.enums.EnumRegistry;
import org.narrative.common.util.html.HTMLStripper;
import org.narrative.common.util.processes.GenericProcess;
import org.narrative.common.util.processes.ProcessManager;
import org.narrative.network.core.area.base.Area;
import org.narrative.network.core.cluster.partition.Partition;
import org.narrative.network.core.cluster.partition.PartitionType;
import org.narrative.network.core.composition.base.Composition;
import org.narrative.network.core.composition.base.CompositionConsumer;
import org.narrative.network.core.composition.base.CompositionType;
import org.narrative.network.core.composition.base.Reply;
import org.narrative.network.core.content.base.Content;
import org.narrative.network.shared.tasktypes.AreaTaskImpl;
import org.narrative.network.shared.tasktypes.CompositionTaskImpl;
import org.narrative.network.shared.tasktypes.GlobalTaskImpl;
import org.narrative.network.shared.tasktypes.TaskRunner;
import org.narrative.network.shared.util.NetworkLogger;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.narrative.common.util.CoreUtils.*;

/**
 * User: barry
 * Date: Sep 13, 2009
 * Time: 11:29:47 PM
 */
public class ReplyIndexHandler extends AreaDataIndexHandlerBase implements CompositionIndexFields {

    private static final NetworkLogger logger = new NetworkLogger(ReplyIndexHandler.class);

    public static final String FIELD_CONTENT_OID = "contentOid";
    public static final String FIELD_COMPOSITION_PARTITION_OID = "compositionPartitionOid";
    public static final String FIELD_COMPOSITION_TYPE = "compositionType";

    public ReplyIndexHandler() {
        super(IndexType.REPLY);
    }

    private static class CompositionListIterator extends SubListIterator<ObjectPair<OID, Integer>> {
        private final int maxReplyChunkSize;

        public CompositionListIterator(List<ObjectPair<OID, Integer>> list, int maxCompositionChunkSize, int maxReplyChunkSize) {
            super(list, maxCompositionChunkSize);
            this.maxReplyChunkSize = maxReplyChunkSize;
        }

        @Override
        protected int getNextChunkSize() {
            int compositions = 0;
            int replies = 0;
            // iterate until we hit the reply limit, or we have enough compositions to fill a full chunk.
            for (int i = getCurrentIndex(); i < getList().size() && compositions < getChunkSize(); i++) {
                ObjectPair<OID, Integer> composition = getList().get(i);
                // if this is the first composition and its count is high enough to be a chunk then just process it
                if (compositions == 0 && composition.getTwo() >= maxReplyChunkSize) {
                    return 1;
                }

                // if this topic will put us over the limit, then break out and dont include this composition, it will be included
                // in the next chunk
                if (replies + composition.getTwo() > maxReplyChunkSize) {
                    break;
                }

                // the topic will not put us over the limit, so lets increment our counters and move to the next one
                replies += composition.getTwo();
                compositions++;
            }

            return compositions;
        }
    }

    @Override
    public void rebuildSolrIndex(long indexVersion) {
        GenericProcess rootProcess = new GenericProcess(ReplyIndexHandler.class.getSimpleName() + "IndexRebuild");
        ProcessManager.getInstance().pushProcess(rootProcess);

        try {
            List<OID> partitionOids = TaskRunner.doRootGlobalTask(new GlobalTaskImpl<List<OID>>(false) {
                @Override
                protected List<OID> doMonitoredTask() {
                    return Partition.dao().getIdsFromObjects(Partition.dao().getAllForType(PartitionType.COMPOSITION));
                }
            });
            for (final OID partitionOid : partitionOids) {
                final SubListIterator<ObjectPair<OID, Integer>> iter = TaskRunner.doRootGlobalTask(new GlobalTaskImpl<SubListIterator<ObjectPair<OID, Integer>>>(false) {
                    @Override
                    protected SubListIterator<ObjectPair<OID, Integer>> doMonitoredTask() {
                        Partition partition = Partition.dao().get(partitionOid);
                        return getNetworkContext().doCompositionTask(partition, new CompositionTaskImpl<SubListIterator<ObjectPair<OID, Integer>>>(false) {
                            @Override
                            protected SubListIterator<ObjectPair<OID, Integer>> doMonitoredTask() {
                                getProcess().updateStatusMessageAndLog(logger, "Getting list of ALL composition oids to reply counts in partition.");

                                return new CompositionListIterator(Composition.dao().getAllCompositionOidsAndReplyCountsForIndexer(), 1000, 150000);
                            }
                        });
                    }
                });

                class ReplyCount {
                    private int totalReplies = 0;
                }
                final ReplyCount stats = new ReplyCount();
                while (iter.hasNext()) {
                    TaskRunner.doRootGlobalTask(new GlobalTaskImpl<Object>(false) {
                        @Override
                        protected Object doMonitoredTask() {
                            final List<OID> compositionOids = ObjectPair.getAllOnes(iter.next());

                            getProcess().updateStatusMessageAndLog(logger, "Processing " + (iter.getCurrentIndex() - compositionOids.size()) + " to " + iter.getCurrentIndex() + " of " + iter.getList().size() + " compositions.");

                            getNetworkContext().doGlobalTask(new GlobalTaskImpl<Object>(false) {
                                @Override
                                protected Object doMonitoredTask() {
                                    // filter out all deleted content ids since we dont need to index the replies if the content is deleted
                                    final Map<OID, Content> contentOidToContent = Content.dao().getIDToObjectsFromObjects(Content.dao().getAllLiveInOids(compositionOids));

                                    final Set<OID> compositionOids = newHashSet();
                                    compositionOids.addAll(contentOidToContent.keySet());
                                    final GenericProcess process = new GenericProcess("Preparing Replies for " + compositionOids.size() + " Compositions");
                                    ProcessManager.getInstance().pushProcess(process);
                                    try {
                                        getNetworkContext().doCompositionTask(Partition.dao().get(partitionOid), new CompositionTaskImpl<Object>(false) {
                                            @Override
                                            protected Object doMonitoredTask() {
                                                List<Reply> replies = Reply.dao().getAllForCompositionOids(compositionOids);
                                                Collection<SolrInputDocument> documents = newLinkedList();

                                                int i = 0;
                                                for (Reply reply : replies) {
                                                    i++;
                                                    stats.totalReplies++;
                                                    SolrInputDocument document;
                                                    if (reply.getComposition().getCompositionType().isContent()) {
                                                        document = createReplySolrInputDocument(contentOidToContent.get(reply.getComposition().getOid()), reply, indexVersion);

                                                    } else {
                                                        logger.warn("Trying to index reply with non-supported content/composition: " + reply.getComposition().getOid());
                                                        document = null;
                                                    }

                                                    if (document != null) {
                                                        documents.add(document);
                                                        addDocumentChunkSafely(documents, reply.getOid());
                                                    }

                                                    if ((i % 500) == 0) {
                                                        process.updateStatusMessageAndLog(logger, "Completed reply index rebuild for " + i + " of " + replies.size() + " replies in chunk.");
                                                    }
                                                }

                                                addDocumentsSafely(documents);

                                                return null;
                                            }
                                        });
                                    } finally {
                                        ProcessManager.getInstance().popProcess();
                                    }

                                    return null;
                                }
                            });

                            return null;
                        }
                    });

                }

                rootProcess.updateStatusMessageAndLog(logger, "Finished reply index rebuild for partition " + partitionOid + ". " + stats.totalReplies + " replies in " + iter.getList().size() + " compositions indexed in work dir.");
            }
        } finally {
            ProcessManager.getInstance().popProcess();
        }
    }

    @Override
    public void auditAndFixMissingItems() {
        logger.warn("auditAndFixMissingItems of reply indexes not yet implemented.");
    }

    @Override
    protected SolrInputDocument createSolrInputDocument(final IndexOperationId id, final long indexVersion, boolean forBulkOperation) {
        final OID partitionOid = id.getExtraDataOid();

        return TaskRunner.doRootGlobalTask(new GlobalTaskImpl<SolrInputDocument>(false) {
            @Override
            protected SolrInputDocument doMonitoredTask() {
                return getNetworkContext().doCompositionTask(Partition.dao().get(partitionOid), new CompositionTaskImpl<SolrInputDocument>(false) {
                    @Override
                    protected SolrInputDocument doMonitoredTask() {
                        OID replyOid = id.getDocId();
                        final Reply reply = Reply.dao().get(replyOid);
                        if (!exists(reply)) {
                            logger.warn("Trying to index non-existent reply: " + replyOid);
                            return null;
                        }
                        Area area = reply.getComposition().getArea();
                        return getNetworkContext().doAreaTask(area, new AreaTaskImpl<SolrInputDocument>(false) {
                            @Override
                            protected SolrInputDocument doMonitoredTask() {
                                assert reply.getComposition().getCompositionType().isContent() : "Expected a Content comment but encountered something unexpected: " + reply.getComposition().getCompositionType();
                                return createReplySolrInputDocument(Content.dao().get(reply.getComposition().getOid()), reply, indexVersion);
                            }
                        });
                    }
                });
            }
        });
    }

    private SolrInputDocument createReplySolrInputDocument(final CompositionConsumer consumer, Reply reply, long indexVersion) {
        if (!exists(consumer)) {
            logger.warn("Trying to index reply with non-existent composition consumer: " + reply.getComposition().getOid());
            return null;
        }

        OID replyOid = reply.getOid();

        // bl: no point in indexing html entities, so set stripEntities to true
        String fullText = HTMLStripper.stripHtmlFragment(reply.getFullText(true), true);

        SolrInputDocument document = createAreaDataDocument(replyOid, reply.getUserOid(), null, fullText, reply.getLiveDatetime(), reply.getLastUpdateDatetime(), consumer.getAuthZone(), consumer.getPortfolio().getArea().getOid(), consumer.getAgeRating(), indexVersion);
        assert consumer.getCompositionType().isContent() : "Encountered unexpected unsupported composition consumer type: " + consumer.getCompositionType();
        Content content = (Content) consumer;

        ContentIndexHandler.addContentDocumentFields(document, content, true);

        document.addField(FIELD_CONTENT_OID, consumer.getOid().getValue());
        document.addField(FIELD_COMPOSITION_PARTITION_OID, consumer.getCompositionPartition().getOid().getValue());
        document.addField(FIELD_COMPOSITION_TYPE, consumer.getCompositionType().getId());
        document.addField(FIELD_HAS_ATTACHMENTS, reply.isHasAttachments());

        return document;
    }

    @Override
    protected SearchResult getResultFromSolrDocument(SolrDocument document, OID oid, int resultIndex) {
        Timestamp liveDatetime = new Timestamp(((Date) document.get(FIELD__COMMON__ITEM_DATE)).getTime());

        return new ReplySearchResult(oid, resultIndex, OID.valueOf(document.get(FIELD_CONTENT_OID)), EnumRegistry.getForId(CompositionType.class, (Integer) document.get(FIELD_COMPOSITION_TYPE)), OID.valueOf(document.get(FIELD_COMPOSITION_PARTITION_OID)), liveDatetime);
    }
}
