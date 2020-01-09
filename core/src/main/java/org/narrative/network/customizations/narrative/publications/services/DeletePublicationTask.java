package org.narrative.network.customizations.narrative.publications.services;

import org.narrative.network.core.area.base.Area;
import org.narrative.network.core.fileondisk.base.services.DeleteFileOnDisk;
import org.narrative.network.core.cluster.partition.PartitionGroup;
import org.narrative.network.core.fileondisk.image.ImageOnDisk;
import org.narrative.network.core.user.User;
import org.narrative.network.customizations.narrative.channels.Channel;
import org.narrative.network.customizations.narrative.channels.ChannelDomain;
import org.narrative.network.customizations.narrative.channels.ChannelUser;
import org.narrative.network.customizations.narrative.channels.DeletedChannel;
import org.narrative.network.customizations.narrative.channels.FollowedChannel;
import org.narrative.network.customizations.narrative.niches.ledgerentries.LedgerEntry;
import org.narrative.network.customizations.narrative.niches.referendum.Referendum;
import org.narrative.network.customizations.narrative.niches.tribunal.TribunalIssue;
import org.narrative.network.customizations.narrative.posts.services.RemoveAllPostsFromChannelTask;
import org.narrative.network.customizations.narrative.publications.Publication;
import org.narrative.network.customizations.narrative.publications.PublicationWaitListEntry;
import org.narrative.network.shared.tasktypes.AreaTaskImpl;
import org.narrative.network.shared.tasktypes.TaskRunner;

import java.util.HashSet;
import java.util.Set;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Date: 2019-09-26
 * Time: 09:49
 *
 * @author jonmark
 */
public class DeletePublicationTask extends AreaTaskImpl<Object> {
    private final Publication publication;
    private final boolean forTribunalRejection;

    public DeletePublicationTask(Publication publication, boolean forTribunalRejection) {
        assert exists(publication) : "We should always have a publication by this point!";

        this.publication = publication;
        this.forTribunalRejection = forTribunalRejection;
    }

    @Override
    protected Object doMonitoredTask() {
        // jw: this is going to be used a lot, so let's just get a reference up front.
        Channel channel = publication.getChannel();

        // jw: the first thing we need to do is gather a bit of information so that we can notify everyone affected.
        String publicationName = publication.getName();
        Set<User> powerUsers = new HashSet<User>();
        powerUsers.addAll(ChannelUser.dao().getUsersWithAnyRoleInChannel(channel));

        // jw: the owner will get a separate email for rejection alerting them to the fact that they are receiving a refund
        //     for the remainder of their service. So, don't include this for tribunalRejection
        if (forTribunalRejection) {
            powerUsers.remove(publication.getOwner());

        } else {
            powerUsers.add(publication.getOwner());
        }

        // jw: next: let's disassociate all content from the publication. Note: this will send an email to all authors
        //     as part of the process. Note: Due to the amount of content being affected and how the emails are processed
        //     we need to process this in its own isolated task.
        TaskRunner.doRootAreaTask(Area.dao().getNarrativePlatformAreaOid(), new AreaTaskImpl<Object>() {
            @Override
            protected Object doMonitoredTask() {
                getAreaContext().doAreaTask(new RemoveAllPostsFromChannelTask(
                        // jw: since we are in a new transaction we need to get a fresh Channel object.
                        Channel.dao().get(channel.getOid()),
                        // jw: this flag is ultimately used by the email, and tells it to use special wording for expired
                        //     publication deletion. By passing false for tribunal rejection we are allowing the email to
                        //     render with it's default "rejection" wording, which is what we want.
                        !forTribunalRejection
                ));
                return null;
            }
        });

        // jw: before we start doing anything destructive, let's create the DeletedChannel entity.
        DeletedChannel deletedChannel = new DeletedChannel(channel);
        DeletedChannel.dao().save(deletedChannel);

        // jw: we will be referencing the deleted channel below in HQL queries, so we need to ensure that hibernate flushes
        //     it into the db prior to that.
        PartitionGroup.getCurrentPartitionGroup().flushAllSessions();

        // jw: with that done, let's clear references to this channel from TribunalIssues
        // note: since there aren't many of these let's process issues and referendums inline.
        for (TribunalIssue issue : TribunalIssue.dao().getAllForChannel(channel)) {
            issue.setChannel(null);
        }

        // jw: similar to above, we need to clean up Referendums and leave a record by means of DeletedChannel
        for (Referendum referendum : Referendum.dao().getAllForPublication(publication)) {
            referendum.setPublication(null);
            referendum.setDeletedChannel(deletedChannel);
        }

        // jw: remove the PublicationWaitListEntry referencing this publication
        PublicationWaitListEntry.dao().deleteForPublication(publication);

        // jw: remove the channel from any referencing LedgerEntries
        LedgerEntry.dao().removeReferencesForDeletedChannel(channel);

        // jw: delete the logo and the header image for the publication to clean up the files.
        {
            // jw: just get a reference up front.
            ImageOnDisk logo = publication.getLogo();
            ImageOnDisk headerImage = publication.getHeaderImage();

            // jw: clear it in case there was one
            publication.setLogo(null);
            publication.setHeaderImage(null);

            // jw: if we had a image, delete it.
            deleteImageOnDisk(logo);
            deleteImageOnDisk(headerImage);
        }

        // jw: remove the FollowedChannel records.
        FollowedChannel.dao().deleteForChannel(channel);

        // jw: remove the ChannelDomain records.
        channel.setPrimaryDomain(null);
        // jw: we need to flush to ensure proper order with Hibernate.
        PartitionGroup.getCurrentPartitionGroup().flushAllSessions();
        ChannelDomain.dao().deleteForChannel(channel);

        // jw: remove the ChannelUser records.
        ChannelUser.dao().deleteForChannel(channel);

        // jw: need to clear the reference from Publication to Channel before we remove the Channel
        publication.setChannel(null);

        // jw: to this point there has been no cross reference betweeen the objects we have been affecting, but we are
        //     getting down into the root level objects. As a result, we should flush to ensure that there are no errors
        //     with the statement order that Hibernate may use. This makes it clear that we need everything before to be
        //     resolved.
        PartitionGroup.getCurrentPartitionGroup().flushAllSessions();

        // jw: remove the channel
        Channel.dao().delete(channel);

        // jw: finally, remove the Publication
        Publication.dao().delete(publication);

        // jw: now that the deletions are all taken care of we should notify all special users, and the owner.
        getAreaContext().doAreaTask(new SendPublicationDeletedEmail(publicationName, powerUsers));

        return null;
    }

    private void deleteImageOnDisk(ImageOnDisk iod) {
        if (exists(iod)) {
            getNetworkContext().doGlobalTask(new DeleteFileOnDisk(iod));
        }
    }
}
