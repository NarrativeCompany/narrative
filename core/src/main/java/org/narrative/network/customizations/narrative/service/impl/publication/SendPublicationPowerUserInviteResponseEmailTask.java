package org.narrative.network.customizations.narrative.service.impl.publication;

import org.narrative.network.core.user.User;
import org.narrative.network.customizations.narrative.niches.services.SendMultiNarrativeEmailTaskBase;
import org.narrative.network.customizations.narrative.publications.Publication;
import org.narrative.network.customizations.narrative.publications.PublicationRole;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Date: 2019-03-16
 * Time: 17:38
 *
 * @author brian
 */
public class SendPublicationPowerUserInviteResponseEmailTask extends SendMultiNarrativeEmailTaskBase {
    private final Publication publication;
    private final User invitee;
    private final Set<PublicationRole> roles;
    private final boolean accepted;

    public SendPublicationPowerUserInviteResponseEmailTask(Publication publication, User invitee, Set<PublicationRole> roles, boolean accepted) {
        this.publication = publication;
        this.invitee = invitee;
        this.roles = roles;
        this.accepted = accepted;
    }

    @Override
    protected Collection<User> getUsers() {
        Set<User> users = new LinkedHashSet<>();
        if(exists(publication.getOwner())) {
            users.add(publication.getOwner());
        }
        users.addAll(publication.getAdmins());
        // bl: don't need to notify the invitee that they've accepted/declined
        users.remove(invitee);
        return users;
    }

    public Publication getPublication() {
        return publication;
    }

    public User getInvitee() {
        return invitee;
    }

    public Set<PublicationRole> getRoles() {
        return roles;
    }

    public boolean isAccepted() {
        return accepted;
    }
}
