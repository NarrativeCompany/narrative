package org.narrative.network.customizations.narrative.service.impl.publication;

import org.narrative.network.core.user.User;
import org.narrative.network.customizations.narrative.publications.Publication;
import org.narrative.network.customizations.narrative.service.api.model.input.UpdateProfileAccountConfirmationInputBase;
import org.narrative.network.customizations.narrative.service.impl.user.UpdateProfileAccountConfirmationBaseTask;

import static org.narrative.network.shared.util.NetworkCoreUtils.*;

/**
 * Date: 9/16/19
 * Time: 2:19 PM
 *
 * @author brian
 */
public class ChangePublicationOwnerTask extends UpdateProfileAccountConfirmationBaseTask<Object> {
    private final Publication publication;
    private final User newOwner;

    public ChangePublicationOwnerTask(Publication publication, User newOwner, User user, UpdateProfileAccountConfirmationInputBase input) {
        super(user, input);
        this.publication = publication;
        this.newOwner = newOwner;
    }

    @Override
    protected Object doMonitoredTask() {
        // bl: just set the owner. that's it!
        // in the future, we may require the new owner to "accept" the ownership transfer, but keeping it simple for now.
        publication.setOwner(newOwner);

        // send a notification email to the new owner to let them know.
        getAreaContext().doAreaTask(new SendPublicationOwnershipChangeToNewOwnerEmailTask(publication, networkContext().getUser()));

        return null;
    }
}
