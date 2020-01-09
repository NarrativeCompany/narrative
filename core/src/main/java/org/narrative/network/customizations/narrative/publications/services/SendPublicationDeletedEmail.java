package org.narrative.network.customizations.narrative.publications.services;

import org.narrative.network.core.user.User;
import org.narrative.network.customizations.narrative.niches.services.SendMultiNarrativeEmailTaskBase;

import java.util.Collection;

/**
 * Date: 2019-09-26
 * Time: 10:55
 *
 * @author jonmark
 */
public class SendPublicationDeletedEmail extends SendMultiNarrativeEmailTaskBase {
    private final String publicationName;
    private final Collection<User> powerUsers;

    public SendPublicationDeletedEmail(String publicationName, Collection<User> powerUsers) {
        this.publicationName = publicationName;
        this.powerUsers = powerUsers;
    }

    @Override
    protected Collection<User> getUsers() {
        return powerUsers;
    }

    public String getPublicationName() {
        return publicationName;
    }
}
