package org.narrative.network.core.search.services;

import org.narrative.common.persistence.OID;
import org.narrative.common.util.IPUtil;
import org.narrative.network.core.cluster.partition.PartitionGroup;
import org.narrative.network.core.content.base.Content;
import org.narrative.network.core.search.IndexOperation;
import org.narrative.network.core.search.IndexType;
import org.narrative.network.shared.tasktypes.TaskRunner;

/**
 * User: barry
 * Date: Sep 17, 2009
 * Time: 11:03:53 AM
 */
public class ContentIndexRunnable implements Runnable {
    private final OID areaOid;
    private final OID contentOid;
    private final OID compositionPartitionOid;
    private final boolean isReindexAllReplies;

    private ContentIndexRunnable(Content content) {
        this.contentOid = content.getOid();
        this.areaOid = content.getArea().getOid();
        this.compositionPartitionOid = content.getCompositionPartitionOid();
        this.isReindexAllReplies = IS_REINDEX_ALL_REPLIES_TOO.get() != null && IS_REINDEX_ALL_REPLIES_TOO.get();
        IS_REINDEX_ALL_REPLIES_TOO.remove();
    }

    @Override
    public void run() {
        IndexType.CONTENT.getIndexHandler().performOperation(IndexOperation.update(contentOid, areaOid));

        if (isReindexAllReplies) {
            TaskRunner.doRootGlobalTask(new IndexAllCompositionRepliesTask(compositionPartitionOid, contentOid));
        }
    }

    private static final ThreadLocal<Boolean> IS_REINDEX_ALL_REPLIES_TOO = new ThreadLocal<Boolean>();

    public static void setReindexAllRepliesWithContent() {
        IS_REINDEX_ALL_REPLIES_TOO.set(true);
        IPUtil.EndOfX.temporaryEndOfThreadThreadLocal.getEndOfX().addRunnable("clearReindexReplies", new Runnable() {
            @Override
            public void run() {
                IS_REINDEX_ALL_REPLIES_TOO.remove();
            }
        });
    }

    public static void registerContentIndexRunnable(Content content) {
        PartitionGroup.addEndOfPartitionGroupRunnable(new ContentIndexRunnable(content));
    }
}
