package org.narrative.network.customizations.narrative.service.impl.publication;

import org.narrative.network.core.user.User;
import org.narrative.network.customizations.narrative.channels.ChannelUser;
import org.narrative.network.customizations.narrative.niches.services.SendMultiNarrativeEmailTaskBase;
import org.narrative.network.customizations.narrative.publications.Publication;
import org.narrative.network.customizations.narrative.publications.PublicationRole;

import java.util.List;

/**
 * Date: 9/20/19
 * Time: 2:59 PM
 *
 * @author brian
 */
public class SendPublicationEditorsEmailTaskBase extends SendMultiNarrativeEmailTaskBase {
    private final Publication publication;

    private List<User> users;

    SendPublicationEditorsEmailTaskBase(Publication publication) {
        this.publication = publication;
    }

    @Override
    protected Object doMonitoredTask() {
        users = ChannelUser.dao().getUsersWithRoleInChannel(publication.getChannel(), PublicationRole.EDITOR);

        return super.doMonitoredTask();
    }

    public Publication getPublication() {
        return publication;
    }

    @Override
    protected List<User> getUsers() {
        return users;
    }
}
