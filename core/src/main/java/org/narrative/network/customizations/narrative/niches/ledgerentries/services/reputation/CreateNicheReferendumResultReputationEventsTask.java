package org.narrative.network.customizations.narrative.niches.ledgerentries.services.reputation;

import org.narrative.common.persistence.OID;
import org.narrative.network.customizations.narrative.niches.ledgerentries.LedgerEntry;
import org.narrative.network.customizations.narrative.niches.ledgerentries.LedgerEntryType;
import org.narrative.network.customizations.narrative.niches.ledgerentries.services.CreateReputationEventsFromLedgerEntryTask;
import org.narrative.network.customizations.narrative.niches.referendum.Referendum;
import org.narrative.network.customizations.narrative.niches.referendum.ReferendumVote;
import org.narrative.shared.event.reputation.DecisionEnum;
import org.narrative.shared.event.reputation.NegativeQualityEventType;
import org.narrative.shared.event.reputation.ReputationEvent;
import org.narrative.shared.event.reputation.VoteEndedEvent;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * Date: 2018-12-14
 * Time: 15:52
 *
 * @author jonmark
 */
public class CreateNicheReferendumResultReputationEventsTask extends CreateReputationEventsFromLedgerEntryTask {

    public CreateNicheReferendumResultReputationEventsTask(LedgerEntry entry) {
        super(entry);
    }

    @Override
    protected LedgerEntryType getExpectedLedgerEntryType() {
        return LedgerEntryType.NICHE_REFERENDUM_RESULT;
    }

    @Override
    protected Collection<ReputationEvent> createEventsFromLedgerEntry(LedgerEntry entry) {
        Referendum referendum = entry.getReferendum();
        boolean passed = referendum.isWasPassed();

        Collection<ReputationEvent> events = new LinkedList<>();

        DecisionEnum decision = passed ? DecisionEnum.ACCEPTED : DecisionEnum.REJECTED;

        Map<OID, Boolean> userVotes = ReferendumVote.dao().getUserVotesForReferendum(referendum);
        Map<Long, DecisionEnum> votes = new HashMap<>();
        for (Map.Entry<OID, Boolean> userVote : userVotes.entrySet()) {
            OID userOid = userVote.getKey();
            boolean votedFor = userVote.getValue();

            // jw: add a vote!
            votes.put(userOid.getValue(), votedFor ? DecisionEnum.ACCEPTED : DecisionEnum.REJECTED);

            // jw: if we have more than 1000 votes, then let's add this as it's own event
            if (votes.size() >= 1000) {
                addVotesEvent(events, votes, referendum, decision);
            }
        }

        // jw: now that we have finished iterating, let's add any remaining votes as a final event.
        // note: this will short out if there are no votes.
        addVotesEvent(events, votes, referendum, decision);

        // if the niche is rejected, record the event for the niche suggester's niche being rejected.
        // bl: only for referendum types that reject an already approved niche.
        if((referendum.getType().isApproveSuggestedNiche() || referendum.getType().isRatifyNiche()) && !passed) {
            events.add(entry.getNicheResolved().getSuggester().getUser().createNegativeQualityEvent(entry.getEventDatetime(), NegativeQualityEventType.NICHE_REJECTED_IN_BALLOT_BOX_OR_APPEAL));
        }

        return events;
    }

    private void addVotesEvent(Collection<ReputationEvent> events, Map<Long, DecisionEnum> votes, Referendum referendum, DecisionEnum decision) {
        // jw: if there is nothing to do then short out.
        if (votes.isEmpty()) {
            return;
        }

        // jw: first, let's add the event
        events.add(VoteEndedEvent.builder()
                .referendumId(referendum.getOid().getValue())
                .decision(decision)
                // jw: create a new map with the values since we will be clearing this map after this is created.
                .userVotesMap(new HashMap<>(votes))
                .build());

        // jw: now that the event is created, let's clear the votes map for the next chunk.
        votes.clear();
    }
}
