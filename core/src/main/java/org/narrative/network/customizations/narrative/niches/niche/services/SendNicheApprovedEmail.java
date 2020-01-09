package org.narrative.network.customizations.narrative.niches.niche.services;

import org.narrative.network.customizations.narrative.niches.niche.Niche;
import org.narrative.network.customizations.narrative.niches.tribunal.TribunalIssue;

/**
 * Created by IntelliJ IDEA.
 * User: jonmark
 * Date: 3/6/18
 * Time: 7:36 AM
 */
public class SendNicheApprovedEmail extends SendNicheStatusChangeEmailBase {
    private final TribunalIssue dueToIssue;

    public SendNicheApprovedEmail(Niche niche, TribunalIssue dueToIssue) {
        super(niche.getSuggester().getUser(), niche);

        // jw: either the niche was just approved from suggested, or brought back via tribunal issue. In either case,
        //     it will go up for sale as its next step!
        assert niche.getStatus().isForSale() : "The niche should be up for sale at this point!";
        // jw: note: due to email preview, the assertion here is only going to be checking for null state
        assert dueToIssue == null || dueToIssue.getType().isApproveRejectedNiche() : "This email should only ever be sent in response to a Approve Rejected Niche tribunal issue.";

        this.dueToIssue = dueToIssue;
    }

    public TribunalIssue getDueToIssue() {
        return dueToIssue;
    }
}
