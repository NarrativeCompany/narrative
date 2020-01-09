package org.narrative.network.customizations.narrative.niches.ledgerentries.services.reputation;

import org.narrative.network.core.user.User;
import org.narrative.network.customizations.narrative.niches.ledgerentries.LedgerEntry;
import org.narrative.network.customizations.narrative.niches.ledgerentries.LedgerEntryType;
import org.narrative.network.customizations.narrative.niches.ledgerentries.services.CreateReputationEventsFromLedgerEntryTask;
import org.narrative.network.customizations.narrative.niches.tribunal.TribunalIssueReport;
import org.narrative.network.customizations.narrative.niches.tribunal.TribunalIssueType;
import org.narrative.shared.event.reputation.ConductEventType;
import org.narrative.shared.event.reputation.NegativeQualityEventType;
import org.narrative.shared.event.reputation.ReputationEvent;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Date: 2018-12-13
 * Time: 09:48
 *
 * @author jonmark
 */
public class CreateIssueResultReputationEventsTask extends CreateReputationEventsFromLedgerEntryTask {

    public CreateIssueResultReputationEventsTask(LedgerEntry entry) {
        super(entry);
    }

    @Override
    protected LedgerEntryType getExpectedLedgerEntryType() {
        return LedgerEntryType.ISSUE_REFERENDUM_RESULT;
    }

    @Override
    protected Collection<ReputationEvent> createEventsFromLedgerEntry(LedgerEntry entry) {
        TribunalIssueType issueType = entry.getIssue().getType();
        boolean passed = entry.getIssue().getReferendum().isWasPassed();

        if (issueType.isApproveNicheDetailChange()) {
            // jw: if the edit was applied then there is nothing to do.
            if (passed) {
                return null;
            }

            // jw: if the edit was rejected then the submitter should take a hit.
            assert entry.getIssue().getTribunalIssueReports().size() == 1 : "The submitter should be recorded as a IssueReport.";
            User submitter = entry.getIssue().getTribunalIssueReports().iterator().next().getReporter().getUser();

            return Collections.singleton(submitter.createNegativeQualityEvent(entry.getEventDatetime(), NegativeQualityEventType.CHANGE_REQUEST_DENIED_BY_TRIBUNAL));
        }

        assert issueType.isApproveRejectedNiche() || issueType.isRatifyNiche() || issueType.isRatifyPublication() : "Unexpected issue type encountered/" + issueType;

        Collection<ReputationEvent> events = new LinkedList<>();

        // jw: if the Niche was rejected, then the suggester should be dinged
        if (issueType.isRatifyNiche()) {
            if (!passed) {
                events.add(entry.getNicheResolved().getSuggester().getUser().createNegativeQualityEvent(entry.getEventDatetime(), NegativeQualityEventType.NICHE_REJECTED_IN_BALLOT_BOX_OR_APPEAL));
            }
        }

        // bl: if the Publication was rejected, then the owner should go conduct negative
        if (issueType.isRatifyPublication()) {
            if (!passed) {
                events.add(entry.getPublicationResolved().getOwner().createConductStatusEvent(entry.getEventDatetime(), ConductEventType.PUBLICATION_REMOVED_FOR_AUP_VIOLATION));
            }
        }

        // jw: we need to remove points from all reporters in this case.
        if (issueType.getReferendumTypeForTribunal().isRatifyStatus() == passed) {
            List<TribunalIssueReport> reports = TribunalIssueReport.dao().getForIssue(entry.getIssue(), 1, Integer.MAX_VALUE);
            if (!isEmptyOrNull(reports)) {
                for (TribunalIssueReport report : reports) {
                    events.add(report.getReporter().getUser().createNegativeQualityEvent(entry.getEventDatetime(), NegativeQualityEventType.APPEAL_NOT_UPHELD_BY_TRIBUNAL));
                }
            }
        }

        return events;
    }
}
