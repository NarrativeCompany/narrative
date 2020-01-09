package org.narrative.network.core.user.services;

import org.narrative.common.persistence.OID;
import org.narrative.common.util.SubListIterator;
import org.narrative.network.core.area.user.AreaUser;
import org.narrative.network.core.area.user.AreaUserRlm;
import org.narrative.network.core.cluster.partition.Partition;
import org.narrative.network.core.cluster.partition.PartitionType;
import org.narrative.network.core.composition.base.Composition;
import org.narrative.network.core.composition.base.CompositionConsumer;
import org.narrative.network.core.composition.base.Reply;
import org.narrative.network.core.content.base.Content;
import org.narrative.network.core.content.base.services.DeleteContentsTask;
import org.narrative.network.core.content.base.services.DeleteReplyFully;
import org.narrative.network.core.user.User;
import org.narrative.network.customizations.narrative.channels.ChannelType;
import org.narrative.network.customizations.narrative.posts.ChannelContent;
import org.narrative.network.customizations.narrative.posts.NarrativePostStatus;
import org.narrative.network.customizations.narrative.publications.Publication;
import org.narrative.network.customizations.narrative.service.impl.publication.SendPublicationWriterAccountDeletedToPublicationEditorsEmailTask;
import org.narrative.network.shared.tasktypes.AllPartitionsTask;
import org.narrative.network.shared.tasktypes.AreaTaskImpl;
import org.narrative.network.shared.tasktypes.CompositionTaskImpl;
import org.narrative.network.shared.tasktypes.GlobalTaskImpl;
import org.narrative.network.shared.tasktypes.TaskOptions;
import org.narrative.network.shared.tasktypes.TaskRunner;
import org.narrative.network.shared.util.NetworkLogger;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.narrative.common.util.CoreUtils.*;
import static org.narrative.network.shared.util.NetworkCoreUtils.*;

/**
 * Created by IntelliJ IDEA.
 * User: martin
 * Date: Apr 15, 2018
 * Time: 1:26:59 PM
 */
public class DeleteUserAccountContentTask extends GlobalTaskImpl<Object> {
    private static final NetworkLogger logger = new NetworkLogger(DeleteUserAccountContentTask.class);

    private final User user;
    private final static int CHUNK_SIZE = 200;

    private final String userName;

    private boolean log;

    public DeleteUserAccountContentTask(User user, String userName) {
        this.user = user;
        this.userName = userName;
    }

    protected Object doMonitoredTask() {
        log = logger.isInfoEnabled();

        for (AreaUser areaUser : user.getAreaUsers()) {
            AreaUserRlm areaUserRlm = AreaUser.getAreaUserRlm(areaUser);

            // bl: send emails to Publication editors that are affected by deleted posts
            // bl: note that these will only send at end of (successful) PartitionGroup
            sendDeletedPostNotificationsToPublicationEditors(areaUserRlm);

            //mk: delete Contents
            execute(new DeleteContentProcess() {
                public String getName() {return "Contents";}

                public List<OID> getObjectOids() {
                    return Content.dao().getAllIDsBy(newNameValuePair(Content.FIELD__AREA_USER_RLM__NAME, areaUserRlm));
                }

                public void deleteChunk(List<OID> chunk) {
                    TaskRunner.doRootAreaTask(areaUser.getArea().getOid(), new DeleteContentsTask(chunk));
                }
            });
        }

        //mk: delete Replies
        PartitionType.COMPOSITION.doTaskInAllPartitionsOfThisType(new TaskOptions(), new AllPartitionsTask<Object>(false) {
            @Override
            protected Object doMonitoredTask() {
                List<OID> replyOids = Reply.dao().getAllIDsBy(newNameValuePair(Reply.FIELD__USER_OID__NAME, user.getOid()));
                if (isEmptyOrNull(replyOids)) {
                    if (log) {
                        logger.info("No replies to delete on partition " + getCurrentPartition().getOid() + " for user /" + user.getOid());
                    }
                } else {
                    if (log) {
                        logger.info("About to delete " + replyOids.size() + " replies on partition " + getCurrentPartition().getOid() + " for user /" + user.getOid());
                    }
                    deleteReplies(getCurrentPartition(), user.getAuthZone().getOid(), replyOids);
                    if (log) {
                        logger.info("Done deleting " + replyOids.size() + " replies on partition " + getCurrentPartition().getOid() + " for user /" + user.getOid());
                    }
                }
                return null;
            }
        });

        return null;
    }

    private void sendDeletedPostNotificationsToPublicationEditors(AreaUserRlm areaUserRlm) {
        Map<OID,Number> channelOidToDeletedPostCount = ChannelContent.dao().getPostCountsByChannelOidForAuthor(ChannelType.PUBLICATION, areaUserRlm, NarrativePostStatus.NON_BLOCKED_STATUSES);
        Map<OID,Publication> oidToPublication = Publication.dao().getIDToObjectsFromIDs(channelOidToDeletedPostCount.keySet());
        for (Map.Entry<OID, Number> entry : channelOidToDeletedPostCount.entrySet()) {
            OID channelOid = entry.getKey();
            Number count = entry.getValue();
            Publication publication = oidToPublication.get(channelOid);
            areaContext().doAreaTask(new SendPublicationWriterAccountDeletedToPublicationEditorsEmailTask(publication, userName, count.intValue()));
        }
    }

    private void deleteReplies(Partition partition, OID areaOid, List<OID> replyOids) {
        for (OID replyOid : replyOids) {
            // bl: do each reply delete in its own transaction to avoid transactional issues with locking CompositionStats
            TaskRunner.doRootAreaTask(areaOid, new AreaTaskImpl<Object>() {
                @Override
                protected Object doMonitoredTask() {
                    getNetworkContext().doCompositionTask(partition, new CompositionTaskImpl<Object>() {
                        @Override
                        protected Object doMonitoredTask() {
                            Reply reply = Reply.dao().get(replyOid);

                            Composition composition = reply.getComposition();
                            CompositionConsumer compositionConsumer = composition.getCompositionConsumer();
                            assert compositionConsumer!=null : "Should always have a CompositionConsumer! How did we miss one? reply/" + reply.getOid();
                            getNetworkContext().doCompositionTask(partition, new DeleteReplyFully(reply, compositionConsumer));
                            return null;
                        }
                    });
                    return null;
                }
            });
        }
    }

    private interface DeleteContentProcess {
        String getName();

        List<OID> getObjectOids();

        void deleteChunk(List<OID> chunk);

    }

    private void execute(DeleteContentProcess process) {
        List<OID> objectOids = process.getObjectOids();
        if (isEmptyOrNull(objectOids)) {
            if (log) {
                logger.info("No " + process.getName() + " to delete for user /" + user.getOid());
            }
        } else {
            if (log) {
                logger.info("About to delete " + objectOids.size() + " " + process.getName() + " for user /" + user.getOid());
            }
            Iterator<List<OID>> iter = new SubListIterator<>(objectOids, CHUNK_SIZE);
            while (iter.hasNext()) {
                process.deleteChunk(iter.next());
            }
            if (log) {
                logger.info("Done deleting " + objectOids.size() + " " + process.getName() + " for user /" + user.getOid());
            }
        }
    }
}