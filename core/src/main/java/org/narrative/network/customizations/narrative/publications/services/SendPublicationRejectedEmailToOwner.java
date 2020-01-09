package org.narrative.network.customizations.narrative.publications.services;

import org.narrative.network.customizations.narrative.niches.services.SendSingleNarrativeEmailTaskBase;
import org.narrative.network.customizations.narrative.niches.tribunal.TribunalIssue;
import org.narrative.network.customizations.narrative.publications.Publication;

/**
 * Date: 2019-08-06
 * Time: 13:25
 *
 * @author jonmark
 */
public class SendPublicationRejectedEmailToOwner extends SendSingleNarrativeEmailTaskBase {
    private final Publication publication;
    private final TribunalIssue dueToIssue;

    public SendPublicationRejectedEmailToOwner(Publication publication, TribunalIssue dueToIssue) {
        super(publication.getOwner());

        this.publication = publication;
        this.dueToIssue = dueToIssue;
    }

    public Publication getPublication() {
        return publication;
    }

    public TribunalIssue getDueToIssue() {
        return dueToIssue;
    }
}
