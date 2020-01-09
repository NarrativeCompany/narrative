package org.narrative.network.core.search.services;

import org.narrative.common.persistence.OID;
import org.narrative.network.core.cluster.partition.Partition;
import org.narrative.network.core.composition.base.Reply;
import org.narrative.network.shared.tasktypes.CompositionTaskImpl;
import org.narrative.network.shared.tasktypes.GlobalTaskImpl;

import java.util.Collections;
import java.util.List;

/**
 * Date: 1/21/13
 * Time: 12:54 PM
 * User: jonmark
 */
public class IndexAllCompositionRepliesTask extends GlobalTaskImpl<Object> {
    private final OID compositionPartitionOid;
    private final OID compositionOid;

    public IndexAllCompositionRepliesTask(OID compositionPartitionOid, OID compositionOid) {
        super(false);
        this.compositionPartitionOid = compositionPartitionOid;
        this.compositionOid = compositionOid;
    }

    @Override
    protected Object doMonitoredTask() {
        Partition compositionPartition = Partition.dao().get(compositionPartitionOid);
        getNetworkContext().doCompositionTask(compositionPartition, new CompositionTaskImpl<Object>(false) {
            @Override
            protected Object doMonitoredTask() {
                List<OID> replyOids = Reply.dao().getAllOidsForCompositionOids(Collections.singletonList(compositionOid));
                if (!replyOids.isEmpty()) {
                    ReplyIndexRunnable.addReplyReindexOperations(replyOids, compositionPartitionOid);
                }
                return null;
            }
        });

        return null;
    }
}
