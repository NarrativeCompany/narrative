package org.narrative.network.customizations.narrative.posts.services;

import org.narrative.common.persistence.OID;
import org.narrative.common.util.SubListIterator;
import org.narrative.network.core.cluster.partition.PartitionGroup;
import org.narrative.network.core.content.base.Content;
import org.narrative.network.customizations.narrative.channels.Channel;
import org.narrative.network.customizations.narrative.posts.ChannelContent;
import org.narrative.network.customizations.narrative.posts.NarrativePostStatus;
import org.narrative.network.customizations.narrative.service.impl.narrativepost.CleanupPostForPublicationRemovalTask;
import org.narrative.network.shared.tasktypes.AreaTaskImpl;
import org.narrative.network.shared.tasktypes.TaskRunner;

import java.util.List;

/**
 * Date: 2019-04-18
 * Time: 16:35
 *
 * @author brian
 */
public class RemoveAllPostsFromChannelTask extends AreaTaskImpl<Object> {
    private final Channel channel;
    private final boolean forChannelDeletion;

    public RemoveAllPostsFromChannelTask(Channel channel, boolean forChannelDeletion) {
        this.channel = channel;
        this.forChannelDeletion = forChannelDeletion;
    }

    @Override
    protected Object doMonitoredTask() {
        // bl: first get a list of all of the content OIDs that are going to be removed from the Niche
        List<OID> contentOids = ChannelContent.dao().getContentOidsInChannelByStatuses(channel, NarrativePostStatus.NON_BLOCKED_STATUSES);

        // jw: before we disassociate this content from the channel we have a bit more work to do for publications
        if (channel.getType().isPublication()) {
            // jw: filter down the list to any content that is moderated or non-active in any way. Since this should be
            //     a small list let's just do the processing here inline.
            List<Content> nonActiveContents = Content.dao().getNonActiveContentFromOidList(contentOids);

            for (Content content : nonActiveContents) {
                getAreaContext().doAreaTask(new CleanupPostForPublicationRemovalTask(content));
            }
        }


        // bl: then, remove all posts from this Niche. note only removing those that are APPROVED.
        // todo: when we do #175 and #179, we need to handle duplicate niche merging here instead of outright removal.
        ChannelContent.dao().removePostsFromChannelByStatuses(channel, NarrativePostStatus.NON_BLOCKED_STATUSES);

        OID areaOid = getAreaContext().getArea().getOid();
        OID channelOid = channel.getOid();

        // finally, we need to notify all of the post authors that their post has been removed from the Niche now.
        // bl: doing this at end of partition group (post-commit) so that we know the published to Niches will be
        // updated on each post since the current transaction that's deleting everything will have already committed.
        PartitionGroup.addEndOfPartitionGroupRunnable(() -> {
            SubListIterator<OID> iter = new SubListIterator<>(contentOids, SubListIterator.CHUNK_SMALL);
            while (iter.hasNext()) {
                List<OID> contentOidChunk = iter.next();
                TaskRunner.doRootAreaTask(areaOid, new AreaTaskImpl<Object>() {
                    @Override
                    protected Object doMonitoredTask() {
                        Channel channel = Channel.dao().get(channelOid);
                        List<Content> contents = Content.dao().getObjectsFromIDsWithCache(contentOidChunk);
                        for (Content content : contents) {
                            // bl: setting moderatorUser to null indicates to the task that it was removed by the system
                            // which implies the niche was rejected.
                            getAreaContext().doAreaTask(new SendPostRemovedFromChannelEmailTask(content, channel, forChannelDeletion, null, false, null));
                        }
                        return null;
                    }
                });
            }
        });

        return null;
    }
}
