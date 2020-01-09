package org.narrative.network.core.composition.base.services;

import org.narrative.common.persistence.OID;
import org.narrative.network.core.cluster.partition.PartitionType;
import org.narrative.network.core.composition.base.Composition;
import org.narrative.network.core.composition.base.CompositionConsumer;
import org.narrative.network.core.composition.files.FilePointer;
import org.narrative.network.core.composition.files.FilePointerSet;
import org.narrative.network.core.content.base.Content;
import org.narrative.network.core.fileondisk.base.FileOnDisk;
import org.narrative.network.core.fileondisk.base.services.DeleteFileOnDisk;
import org.narrative.network.core.rating.model.UserAgeRatedComposition;
import org.narrative.network.core.rating.model.UserQualityRatedComposition;
import org.narrative.network.core.rating.model.UserQualityRatedReply;
import org.narrative.network.shared.tasktypes.AreaTaskImpl;
import org.narrative.network.shared.tasktypes.CompositionTaskImpl;

import java.util.Collection;
import java.util.List;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Created by IntelliJ IDEA.
 * User: Paul
 * Date: Feb 17, 2006
 * Time: 3:44:55 PM
 */
public class DeleteCompositionConsumer extends AreaTaskImpl<Object> {
    private final CompositionConsumer compositionConsumer;

    public DeleteCompositionConsumer(CompositionConsumer compositionConsumer) {
        this.compositionConsumer = compositionConsumer;
    }

    protected Object doMonitoredTask() {
        assert exists(compositionConsumer) : "Content cannot be null and must exist in the db";

        //delete the composition, but leave the transaction open until all transactions have been completed.
        if (compositionConsumer.isHasComposition()) {
            getNetworkContext().doCompositionTask(compositionConsumer.getCompositionPartition(), new CompositionTaskImpl<Object>() {
                protected Integer doMonitoredTask() {
                    Composition composition = compositionConsumer.getCompositionCache().getComposition();
                    if (exists(composition)) {
                        Collection<OID> fileOnDiskOidsToDelete = newLinkedList();

                        // if we need to delete the global content, then delete the composition and all replies
                        // bl: ContentConsumer now lives down in the composition database, so delete
                        // that here, too.  note that the lifecycle of the ContentConsumer
                        // is the same as the lifecycle of the Composition record.
                        // delete the content comsumer.  That should cascade to FilePointerSets accordingly.
                        // nb. should cascade to Composition as well.
                        //Composition.dao().delete(composition);
                        for (FilePointerSet filePointerSet : composition.getFilePointerSets()) {
                            for (FilePointer filePointer : (List<FilePointer>) filePointerSet.getFilePointerList()) {
                                fileOnDiskOidsToDelete.add(filePointer.getFileOnDiskOid());
                            }
                        }

                        // bl: since the Composition.filePointerSet is a OneToOne association (due to the fact that
                        // FilePointerSet.composition is a ManyToOne), we have to manually delete the FilePointerSet
                        // associated with the Composition first. then, we'll let the remaining FilePointerSets be
                        // deleted automatically as part of the cascade-on-delete for Reply. no longer cascading
                        // on delete for the Composition.filePointerSets collection, which is what was causing
                        // batch update row count unexpected errors.
                        FilePointerSet fps = composition.getFilePointerSet();
                        if (exists(fps)) {
                            composition.setFilePointerSet(null);
                            FilePointerSet.dao().delete(fps);
                        }

                        if (compositionConsumer.getCompositionType().isContent()) {
                            Content content = cast(compositionConsumer, Content.class);

                            // jw: avatars are referenced attachments now, so let's just remove the reference to ImageOnDisk
                            //     before we delete the FileOnDisks below.
                            if (content.getContentType().isSupportsTitleImage()) {
                                content.setAvatarImageOnDisk(null);
                            }
                        }

                        // bl: the cascade delete isn't working very well. it seems to only delete a single row. it's also not very efficient.
                        // so, let's delete all of the rated records in bulk queries
                        UserQualityRatedComposition.dao().deleteAllByPropertyValue(UserQualityRatedComposition.Fields.composition, composition);
                        UserAgeRatedComposition.dao().deleteAllByPropertyValue(UserAgeRatedComposition.Fields.composition, composition);
                        UserQualityRatedReply.dao().deleteAllForComposition(composition);

                        // jw: Now that we have cleaned up all ancillary data for the Consumer let's go ahead and delete
                        //     the Composition. That should cascade to all sub-entities data.
                        Composition.dao().delete(composition);

                        if (!fileOnDiskOidsToDelete.isEmpty()) {
                            List<FileOnDisk> fileOnDisks = FileOnDisk.dao().getObjectsFromIDs(fileOnDiskOidsToDelete);
                            for (FileOnDisk fileOnDisk : fileOnDisks) {
                                getNetworkContext().doGlobalTask(new DeleteFileOnDisk(fileOnDisk));
                            }
                        }
                    }

                    return null;
                }
            });
        }

        // todo: do we need to track FileOnDisk candidates for deletion?  if so, need to do something like:
        // note: it would be a lot better to do something like this via a single query!
        /*Set<OID> fileOnDiskOidDeletionCandidates = new HashSet<OID>();
        Collection<FilePointerSet> filePointerSets = content.getFilePointerSets();
        for (FilePointerSet filePointerSet : filePointerSets) {
            for (FilePointer filePointer : filePointerSet.getFilePointerList()) {
                fileOnDiskOidDeletionCandidates.add(filePointer.getFileOnDiskOid());
            }
        }*/

        // delete the Content.  should automatically cascade to all Content associations.
        // so this should also handle deletion of reply attachment file pointer sets.
        compositionConsumer.getCompositionType().getDAO().delete(compositionConsumer);

        // flush the sessions to make sure the deletion is going to work.
        PartitionType.flushAllOpenSessionsForCurrentPartitionGroup();

        return null;
    }

}
