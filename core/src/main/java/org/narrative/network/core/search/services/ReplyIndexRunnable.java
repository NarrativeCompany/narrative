package org.narrative.network.core.search.services;

import org.narrative.common.persistence.OID;
import org.narrative.network.core.search.IndexOperation;
import org.narrative.network.core.search.IndexType;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Date: Oct 14, 2009
 * Time: 2:00:30 PM
 *
 * @author Jonmark Weber
 */
public class ReplyIndexRunnable implements Runnable {
    private final OID partitionOid;
    private final List<OID> replyOids;

    public ReplyIndexRunnable(List<OID> replyOids, OID partitionOid) {
        this.partitionOid = partitionOid;
        this.replyOids = replyOids;
    }

    public ReplyIndexRunnable(OID replyOid, OID partitionOid) {
        this(Collections.singletonList(replyOid), partitionOid);
    }

    @Override
    public void run() {
        addReplyReindexOperations(replyOids, partitionOid);
    }

    public static void addReplyReindexOperations(Collection<OID> replyOids, OID partitionOid) {
        for (OID replyOid : replyOids) {
            IndexType.REPLY.getIndexHandler().performOperation(IndexOperation.update(replyOid, partitionOid));
        }
    }
}
