package org.narrative.network.customizations.narrative.service.impl.publication;

import org.narrative.network.core.user.User;
import org.narrative.network.customizations.narrative.niches.services.SendSingleNarrativeEmailTaskBase;
import org.narrative.network.customizations.narrative.publications.Publication;
import org.narrative.network.customizations.narrative.publications.PublicationRole;

/**
 * Date: 2019-03-16
 * Time: 17:38
 *
 * @author brian
 */
public class SendPublicationPowerUserRemovedEmailTask extends SendSingleNarrativeEmailTaskBase {
    private final Publication publication;
    private final User admin;
    private final PublicationRole role;
    private final boolean wasInvitation;

    public SendPublicationPowerUserRemovedEmailTask(Publication publication, User admin, User user, PublicationRole role, boolean wasInvitation) {
        super(user);
        this.publication = publication;
        this.admin = admin;
        this.role = role;
        this.wasInvitation = wasInvitation;
    }

    public Publication getPublication() {
        return publication;
    }

    public User getAdmin() {
        return admin;
    }

    public PublicationRole getRole() {
        return role;
    }

    public boolean isWasInvitation() {
        return wasInvitation;
    }
}
