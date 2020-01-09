package org.narrative.network.core.search.services;

import org.narrative.common.persistence.OID;
import org.narrative.common.util.SubListIterator;
import org.narrative.network.core.area.portfolio.Portfolio;
import org.narrative.network.core.cluster.partition.Partition;
import org.narrative.network.core.composition.base.Composition;
import org.narrative.network.core.composition.base.CompositionConsumer;
import org.narrative.network.core.composition.base.CompositionType;
import org.narrative.network.core.composition.base.Reply;
import org.narrative.network.core.content.base.Content;
import org.narrative.network.core.content.base.ContentType;
import org.narrative.network.core.search.IndexType;
import org.narrative.network.core.search.MessageSearchResult;
import org.narrative.network.core.search.ReplySearchResult;
import org.narrative.network.shared.tasktypes.CompositionTaskImpl;
import org.narrative.network.shared.util.NetworkLogger;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.narrative.common.util.CoreUtils.*;
import static org.narrative.network.shared.util.NetworkCoreUtils.*;

/**
 * Date: 2/23/11
 * Time: 9:48 AM
 *
 * @author brian
 */
public class ContentSearchCriteria extends SearchCriteria {
    private static final NetworkLogger logger = new NetworkLogger(ContentSearchCriteria.class);

    public ContentSearchCriteria(Portfolio portfolio, boolean includeReplies, Set<ContentType> contentTypes) {
        super(portfolio, includeReplies ? Arrays.asList(IndexType.CONTENT, IndexType.REPLY) : Collections.singleton(IndexType.CONTENT), EnumSet.of(CompositionType.CONTENT));

        assert !isEmptyOrNull(contentTypes) : "Must supply ContentTypes to limit the search to!";

        assert contentTypes.size() == 1 : "Should only ever search for content types individually!";
    }

    @Override
    public boolean isCriteriaSpecified() {
        return super.isCriteriaSpecified();
    }

    public static Map<CompositionType, Set<OID>> getRealmPartitionOidToConsumerOids(List<MessageSearchResult> searchResults) {
        Map<CompositionType, Set<OID>> compositionTypeLookup = newHashMap();
        for (MessageSearchResult searchResult : searchResults) {
            Set<OID> consumerOids = compositionTypeLookup.get(searchResult.getCompositionType());
            if (consumerOids == null) {
                compositionTypeLookup.put(searchResult.getCompositionType(), consumerOids = newHashSet());
            }
            consumerOids.add(searchResult.getCompositionConsumerOid());
        }

        return compositionTypeLookup;
    }

    public static void prefetchMessageResultChunk(Map<OID, ? extends CompositionConsumer> consumerOidToConsumer, List<MessageSearchResult> searchResults) {
        final Map<OID, ReplySearchResult> replyOidToReplySearchResult = newHashMap();
        Map<OID, Set<OID>> compositionPartitionOidToReplyOids = newHashMap();
        for (MessageSearchResult searchResult : searchResults) {
            CompositionConsumer consumer = consumerOidToConsumer.get(searchResult.getCompositionConsumerOid());
            if (!exists(consumer)) {
                continue;
            }
            searchResult.setCompositionConsumer(consumer);
            if (searchResult.getIndexType().isReply()) {
                ReplySearchResult replySearchResult = (ReplySearchResult) searchResult;
                replyOidToReplySearchResult.put(replySearchResult.getOid(), replySearchResult);
                Set<OID> replyOids = compositionPartitionOidToReplyOids.get(replySearchResult.getCompositionPartitionOid());
                if (replyOids == null) {
                    compositionPartitionOidToReplyOids.put(replySearchResult.getCompositionPartitionOid(), replyOids = newHashSet());
                }
                replyOids.add(replySearchResult.getOid());
            }
        }

        final Map<OID, Reply> replyOidToReply = newHashMap();
        for (OID partitionOid : compositionPartitionOidToReplyOids.keySet()) {
            final Set<OID> replyOids = compositionPartitionOidToReplyOids.get(partitionOid);
            networkContext().doCompositionTask(Partition.dao().get(partitionOid), new CompositionTaskImpl<Object>(false) {
                @Override
                protected Object doMonitoredTask() {
                    List<Reply> replies = Reply.dao().getViewableRepliesForReplyOids(replyOids);

                    replyOidToReply.putAll(Reply.dao().getIDToObjectsFromObjects(replies));
                    return null;
                }
            });
        }

        for (ReplySearchResult replySearchResult : replyOidToReplySearchResult.values()) {
            Reply reply = replyOidToReply.get(replySearchResult.getOid());
            if (exists(reply)) {
                replySearchResult.setReply(reply);
            }
        }
    }

    // lets expose this functionality for the EverythingSearcherTask
    public static void finalizeMessageSearchResults(List<MessageSearchResult> searchResultList, SearchCriteria criteria) {
        if (!isEmpty(criteria.getQueryString()) || !isEmpty(criteria.getRequiredWords()) || !isEmpty(criteria.getExactPhrase()) || !isEmpty(criteria.getOptionalWords())) {
            Map<OID, Set<OID>> compositionPartitionOidToCompositionOids = newHashMap();
            for (MessageSearchResult messageSearchResult : searchResultList) {
                Set<OID> compositionOids = compositionPartitionOidToCompositionOids.get(messageSearchResult.getCompositionConsumer().getCompositionPartition().getOid());
                if (compositionOids == null) {
                    compositionPartitionOidToCompositionOids.put(messageSearchResult.getCompositionConsumer().getCompositionPartition().getOid(), compositionOids = newHashSet());
                }
                compositionOids.add(messageSearchResult.getOid());
            }
            final Map<OID, String> compositionOidToBody = newHashMap();
            for (OID partitionOid : compositionPartitionOidToCompositionOids.keySet()) {
                final Set<OID> compositionOids = compositionPartitionOidToCompositionOids.get(partitionOid);
                networkContext().doCompositionTask(Partition.dao().get(partitionOid), new CompositionTaskImpl<Object>(false) {
                    @Override
                    protected Object doMonitoredTask() {
                        compositionOidToBody.putAll(Composition.dao().getCompositionOidToBody(compositionOids));
                        return null;
                    }
                });
            }
        }
    }

    public static Map<OID, ? extends CompositionConsumer> getCompositionOidToConsumerMapForData(Map<CompositionType, Set<OID>> groupedConsumerOids) {
        if (groupedConsumerOids.isEmpty()) {
            return Collections.emptyMap();
        }
        // bl: we can assume we are already in the scope of the proper realm partition here
        Map<OID, CompositionConsumer> results = newHashMap();
        for (Map.Entry<CompositionType, Set<OID>> typeToOids : groupedConsumerOids.entrySet()) {
            addCompositionConsumersLookup(typeToOids.getKey(), typeToOids.getValue(), results);
        }

        return results;
    }

    private static void addCompositionConsumersLookup(CompositionType compositionType, Set<OID> consumerOids, Map<OID, CompositionConsumer> results) {
        SubListIterator<OID> oidIterator = newSubListIterator(newLinkedList(consumerOids), 1000);
        while (oidIterator.hasNext()) {
            List<OID> oids = oidIterator.next();

            assert compositionType.isContent() : "Expected Content composition type but got: " + compositionType;
            results.putAll(Content.dao().getIDToObjectsFromObjects(Content.dao().removeNonExistentObjects(Content.dao().getObjectsFromIDsWithCache(oids))));
        }
    }

}
