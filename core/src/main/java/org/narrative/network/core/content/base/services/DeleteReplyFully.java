package org.narrative.network.core.content.base.services;

import org.narrative.common.persistence.OID;
import org.narrative.network.core.composition.base.CompositionConsumer;
import org.narrative.network.core.composition.base.CompositionConsumerStats;
import org.narrative.network.core.composition.base.Reply;
import org.narrative.network.core.composition.files.FilePointer;
import org.narrative.network.core.content.base.Content;
import org.narrative.network.core.fileondisk.base.FileOnDisk;
import org.narrative.network.core.fileondisk.base.services.DeleteFileOnDisk;
import org.narrative.network.core.rating.model.UserQualityRatedReply;
import org.narrative.network.shared.tasktypes.CompositionTaskImpl;

import java.util.List;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Created by IntelliJ IDEA.
 * User: Paul
 * Date: Sep 17, 2007
 * Time: 3:05:25 PM
 * To change this template use File | Settings | File Templates.
 */
public class DeleteReplyFully extends CompositionTaskImpl<Object> {

    private final Reply reply;
    private final CompositionConsumer compositionConsumer;

    public DeleteReplyFully(Reply reply, CompositionConsumer compositionConsumer) {
        this.reply = reply;
        this.compositionConsumer = compositionConsumer;
    }

    protected Object doMonitoredTask() {

        List<OID> fileOnDiskOidsToDelete = newLinkedList();
        List<OID> deletedFilePointerOids = newLinkedList();
        // jw: first things first, lets delete any FileOnDisk records for this reply.
        if (exists(reply.getFilePointerSet())) {
            for (FilePointer filePointer : (List<FilePointer>) reply.getFilePointerSet().getFilePointerList()) {
                fileOnDiskOidsToDelete.add(filePointer.getFileOnDiskOid());
                deletedFilePointerOids.add(filePointer.getOid());
            }
        }

        // bl: before we delete the reply, delete all of the rated reply objects
        UserQualityRatedReply.dao().deleteAllByPropertyValue(UserQualityRatedReply.Fields.reply, reply);

        // delete the reply.
        // nb. will cascade correctly to ReplyFilePointerSet.
        // jw: this should trigger the delete index from the Listener
        Reply.dao().updateCompositionStatsForReplyVisibilityChange(reply, true);

        // jw: now that the reply has been deleted we should be safe to delete the file on disks!
        if (!fileOnDiskOidsToDelete.isEmpty()) {
            List<FileOnDisk> fileOnDisksToDelete = FileOnDisk.dao().getObjectsFromIDs(fileOnDiskOidsToDelete);
            for (FileOnDisk fileOnDisk : fileOnDisksToDelete) {
                getNetworkContext().doGlobalTask(new DeleteFileOnDisk(fileOnDisk));
            }
        }

        //update the content stats for all the areas
        {
            // bl: the following line was resulting in weird "collection [org.narrative.network.core.content.base.Content.contentTags] was not processed by flush()"
            // errors.  also happening for Content.contentReports and AreaUserRlm.filters.  for whatever reason
            // (probably a Hibernate bug), getting the ContentStatsForUpdate off of the Content solves the problem.
            //ContentStats cs = ContentStats.dao().get(contentOid, LockMode.PESSIMISTIC_WRITE);
            // bl: lock the Content object up front for update if this is for Content. otherwise, if we lock ContentStats
            // first and then try to lock Content after, the ContentStats will be re-read from the database as
            // part of the Content object refresh (since Content.contentStats is eagerly fetched). our dirty check
            // behavior in dao().lock() will then fail, causing exceptions.
            Content content = compositionConsumer.getCompositionType().isContent() ? Content.dao().lock(cast(compositionConsumer, Content.class)) : null;
            CompositionConsumerStats stats = compositionConsumer.getCompositionType().isSupportsStats() ? compositionConsumer.getStatsForUpdate() : null;

            if (stats != null) {
                stats.syncStats(reply.getComposition().getCompositionStats());
            }
        }

        return null;
    }
}
