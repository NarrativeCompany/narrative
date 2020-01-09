package org.narrative.network.customizations.narrative.niches.niche.services;

import org.narrative.network.core.user.User;
import org.narrative.network.customizations.narrative.niches.niche.Niche;
import org.narrative.network.customizations.narrative.niches.services.SendSingleNarrativeEmailTaskBase;

/**
 * Created by IntelliJ IDEA.
 * User: jonmark
 * Date: 3/6/18
 * Time: 7:29 AM
 */
public class SendNicheStatusChangeEmailBase extends SendSingleNarrativeEmailTaskBase {
    private Niche niche;

    protected SendNicheStatusChangeEmailBase(User user, Niche niche) {
        super(user);

        this.niche = niche;
    }

    public Niche getNiche() {
        return niche;
    }
}
