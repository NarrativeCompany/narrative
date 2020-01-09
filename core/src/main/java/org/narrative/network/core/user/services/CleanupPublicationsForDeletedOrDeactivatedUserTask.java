package org.narrative.network.core.user.services;

import org.narrative.network.core.user.User;
import org.narrative.network.customizations.narrative.channels.ChannelUser;
import org.narrative.network.customizations.narrative.publications.Publication;
import org.narrative.network.shared.tasktypes.AreaTaskImpl;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * User: brian
 * Date: 9/23/19
 * Time: 8:27 AM
 */
public class CleanupPublicationsForDeletedOrDeactivatedUserTask extends AreaTaskImpl<Object> {
    private final User user;

    public CleanupPublicationsForDeletedOrDeactivatedUserTask(User user) {
        this.user = user;
    }

    @Override
    protected Object doMonitoredTask() {
        List<Publication> publications = Publication.dao().getOwnedPublications(user);

        // bl: just clear out the Publication owner
        for (Publication publication : publications) {
            publication.setOwner(null);
        }

        // bl: need to also remove all ChannelUsers
        Collection<ChannelUser> channelUsers = user.getChannelUsers().values();
        Iterator<ChannelUser> iter = channelUsers.iterator();
        while (iter.hasNext()) {
            ChannelUser channelUser = iter.next();
            iter.remove();
            ChannelUser.dao().delete(channelUser);
        }

        return null;
    }
}
