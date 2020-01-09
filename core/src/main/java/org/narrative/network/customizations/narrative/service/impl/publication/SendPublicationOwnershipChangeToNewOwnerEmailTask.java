package org.narrative.network.customizations.narrative.service.impl.publication;

import org.narrative.network.core.user.User;
import org.narrative.network.customizations.narrative.niches.services.SendSingleNarrativeEmailTaskBase;
import org.narrative.network.customizations.narrative.publications.Publication;

/**
 * Date: 2019-03-16
 * Time: 17:38
 *
 * @author brian
 */
public class SendPublicationOwnershipChangeToNewOwnerEmailTask extends SendSingleNarrativeEmailTaskBase {
    private final Publication publication;
    private final User originalOwner;

    public SendPublicationOwnershipChangeToNewOwnerEmailTask(Publication publication, User originalOwner) {
        super(publication.getOwner());
        this.publication = publication;
        this.originalOwner = originalOwner;
    }

    public Publication getPublication() {
        return publication;
    }

    public User getOriginalOwner() {
        return originalOwner;
    }
}
