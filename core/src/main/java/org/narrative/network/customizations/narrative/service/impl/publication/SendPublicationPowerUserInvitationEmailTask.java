package org.narrative.network.customizations.narrative.service.impl.publication;

import org.narrative.network.core.user.User;
import org.narrative.network.customizations.narrative.niches.services.SendSingleNarrativeEmailTaskBase;
import org.narrative.network.customizations.narrative.publications.Publication;
import org.narrative.network.customizations.narrative.publications.PublicationRole;

import java.util.EnumSet;
import java.util.Set;

/**
 * Date: 2019-03-16
 * Time: 17:38
 *
 * @author brian
 */
public class SendPublicationPowerUserInvitationEmailTask extends SendSingleNarrativeEmailTaskBase {
    private final Publication publication;
    private final User inviter;
    private final Set<PublicationRole> invitedRoles;

    public SendPublicationPowerUserInvitationEmailTask(Publication publication, User inviter, User invitee, Set<PublicationRole> invitedRoles) {
        super(invitee);
        this.publication = publication;
        this.inviter = inviter;
        // bl: use an EnumSet for the invited roles so that the natural order is maintained
        this.invitedRoles = EnumSet.copyOf(invitedRoles);
    }

    public Publication getPublication() {
        return publication;
    }

    public User getInviter() {
        return inviter;
    }

    public Set<PublicationRole> getInvitedRoles() {
        return invitedRoles;
    }
}
