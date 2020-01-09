package org.narrative.network.customizations.narrative.publications.services;

import org.narrative.network.customizations.narrative.niches.services.SendSingleNarrativeEmailTaskBase;
import org.narrative.network.customizations.narrative.publications.Publication;
import org.narrative.network.customizations.narrative.publications.PublicationExpiringReminderType;

/**
 * Date: 2019-08-09
 * Time: 13:50
 *
 * @author jonmark
 */
public class SendPublicationExpiringReminderEmail extends SendSingleNarrativeEmailTaskBase {
    private final Publication publication;
    private final PublicationExpiringReminderType reminderType;

    public SendPublicationExpiringReminderEmail(Publication publication, PublicationExpiringReminderType reminderType) {
        super(publication.getOwner());

        this.publication = publication;
        this.reminderType = reminderType;
    }

    public Publication getPublication() {
        return publication;
    }

    public PublicationExpiringReminderType getReminderType() {
        return reminderType;
    }
}
