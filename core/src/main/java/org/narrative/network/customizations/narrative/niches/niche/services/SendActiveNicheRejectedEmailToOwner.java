package org.narrative.network.customizations.narrative.niches.niche.services;

import org.narrative.network.core.user.User;
import org.narrative.network.customizations.narrative.niches.niche.Niche;

/**
 * Created by IntelliJ IDEA.
 * User: jonmark
 * Date: 3/6/18
 * Time: 7:38 AM
 */
public class SendActiveNicheRejectedEmailToOwner extends SendNicheStatusChangeEmailBase {
    public SendActiveNicheRejectedEmailToOwner(User originalOwner, Niche niche) {
        super(originalOwner, niche);

        assert niche.getStatus().isRejected() : "The niche should be rejected at this point!";
    }

    @Override
    public boolean isAlwaysSendEmail() {
        // bl: always send the email to niche owners when the niche is rejected due to tribunal appeal
        return true;
    }
}
