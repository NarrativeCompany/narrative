package org.narrative.network.customizations.narrative.niches.niche.services;

import org.narrative.network.customizations.narrative.niches.niche.Niche;

/**
 * Created by IntelliJ IDEA.
 * User: jonmark
 * Date: 3/6/18
 * Time: 7:38 AM
 */
public class SendSuggestedNicheRejectedEmail extends SendNicheStatusChangeEmailBase {
    public SendSuggestedNicheRejectedEmail(Niche niche) {
        super(niche.getSuggester().getUser(), niche);

        assert niche.getStatus().isRejected() : "The niche should be rejected at this point!";
    }
}
