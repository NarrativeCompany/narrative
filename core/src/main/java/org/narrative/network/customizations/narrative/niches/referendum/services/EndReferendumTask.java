package org.narrative.network.customizations.narrative.niches.referendum.services;

import org.narrative.network.core.user.User;
import org.narrative.network.customizations.narrative.channels.Channel;
import org.narrative.network.customizations.narrative.niches.ledgerentries.LedgerEntry;
import org.narrative.network.customizations.narrative.niches.ledgerentries.LedgerEntryType;
import org.narrative.network.customizations.narrative.niches.ledgerentries.services.SaveLedgerEntryTask;
import org.narrative.network.customizations.narrative.niches.niche.Niche;
import org.narrative.network.customizations.narrative.niches.niche.services.PutNicheOnSaleTask;
import org.narrative.network.customizations.narrative.niches.niche.services.RejectNicheTask;
import org.narrative.network.customizations.narrative.niches.referendum.Referendum;
import org.narrative.network.customizations.narrative.niches.referendum.ReferendumType;
import org.narrative.network.customizations.narrative.niches.referendum.ReferendumVote;
import org.narrative.network.customizations.narrative.niches.referendum.metadata.NicheDetailChangeReferendumMetadata;
import org.narrative.network.customizations.narrative.publications.Publication;
import org.narrative.network.customizations.narrative.publications.services.RejectPublicationTask;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;

import static org.narrative.common.util.CoreUtils.*;
import static org.narrative.network.shared.util.NetworkCoreUtils.*;

/**
 * Created by IntelliJ IDEA.
 * User: jonmark
 * Date: 2/27/18
 * Time: 8:03 AM
 */
public class EndReferendumTask extends UpdateReferendumTask {
    private final BigDecimal totalVotePoints;
    private final BigDecimal totalVotePointsFor;

    private static final Duration LOCK_STATUS_RELATED_TRIBUNAL_ISSUES_DURATION = Duration.ofDays(30);

    public EndReferendumTask(Referendum referendum) {
        this(referendum, BigDecimal.valueOf(referendum.getTotalVotePoints()), BigDecimal.valueOf(referendum.getVotePointsFor()));
    }

    public EndReferendumTask(Referendum referendum, BigDecimal totalVotePoints, BigDecimal totalVotePointsFor) {
        super(referendum);
        this.totalVotePoints = totalVotePoints;
        this.totalVotePointsFor = totalVotePointsFor;
    }

    @Override
    protected void updateReferendum(Referendum referendum) {
        // jw: the referendum is now closed one way or another.
        referendum.setOpen(false);

        // jw: let's create the referendum result LedgerEntry, since that is the same either way
        boolean forTribunalIssue = referendum.getType().isTribunalReferendum();
        LedgerEntry resultEntry = new LedgerEntry(null, forTribunalIssue ? LedgerEntryType.ISSUE_REFERENDUM_RESULT : LedgerEntryType.NICHE_REFERENDUM_RESULT);
        // jw: to ensure that this event appears below any other events that may be created as part of the result processing,
        //     let's set it back 1 millisecond.
        resultEntry.setEventDatetime(Instant.now().minusMillis(1));

        Channel channel = referendum.getChannel();
        resultEntry.setReferendum(referendum);
        resultEntry.setChannel(channel);
        if (forTribunalIssue) {
            resultEntry.setIssue(referendum.getTribunalIssue());

            // jw: let's go ahead and persist the tribunal members who did not participate in this vote now!
            for (ReferendumVote vote : referendum.createVotesForInactiveTribunalMembers()) {
                ReferendumVote.dao().save(vote);
            }
        }
        networkContext().doGlobalTask(new SaveLedgerEntryTask(resultEntry));

        // jw: since the owner could be become dissassociated after these changes, let's go ahead and cache them up front.
        User owner = channel.getConsumer().getChannelOwner();

        // jw: time to actually process the referendum, so lets pass that along appropriately now.
        boolean wasPassed = referendum.getType().wasReferendumPassed(totalVotePoints, totalVotePointsFor);
        if (wasPassed) {
            handlePassedReferendum(referendum);

        } else {
            handleFailedReferendum(referendum);
        }

        // jw: regardless of pass or failure, if the tribunal ended a status related tribunal issue, we need to lock it
        //     so that another one cannot be raised until an appropriate amount of time has passed.
        if (referendum.getType().isRatifyNiche() || referendum.getType().isTribunalApproveRejectedNiche()) {
            referendum.getChannel().setStatusRelatedTribunalIssuesLockedUntilDatetime(Instant.now().plus(LOCK_STATUS_RELATED_TRIBUNAL_ISSUES_DURATION));
        }

        // jw: now that the referendum handling is taken care of above, lets send the corresponding email to the owner
        // jw: we do not want to send this email to the owner if the tribunal rejected a publication. They will get a separate
        //     and more explicit email focused on that scenario as part of the handling code.
        if (exists(owner) && (wasPassed || !referendum.getType().isTribunalRatifyPublication())) {
            assert referendum.getType().isTribunalReferendum() : "There should only be an owner for Tribunal referendums! referendum/" + referendum.getOid() + " type/" + referendum.getType();
            getAreaContext().doAreaTask(new SendReferendumResultToOwnerEmail(referendum, wasPassed, owner));
        }

        // jw: now that this referendum has ended, let's schedule the email to send its results.
        ReferendumResultsInstantEmailJob.schedule(referendum);
    }

    private void handlePassedReferendum(Referendum referendum) {
        ReferendumType type = referendum.getType();
        Niche niche = referendum.getNiche();

        if (type.isTribunalReferendum()) {
            // bl: set the status to null to indicate the appeal is closed
            referendum.getTribunalIssue().setStatus(null);
            if (type.isTribunalApproveNicheDetails()) {
                NicheDetailChangeReferendumMetadata detailsMetadata = referendum.getMetadata();
                if (detailsMetadata.isWasNameChanged()) {
                    niche.updateName(detailsMetadata.getNewName());
                }
                if (detailsMetadata.isWasDescriptionChanged()) {
                    niche.setDescription(detailsMetadata.getNewDescription());
                }
                // jw: need to re-index this in Solr, now that the name/description have changed.
                niche.updateSolrIndex();

            } else if (type.isTribunalApproveRejectedNiche()) {
                // bl: if the tribunal approved the rejected niche, then immediately put it up for sale!
                getAreaContext().doAreaTask(new PutNicheOnSaleTask(niche, referendum.getTribunalIssue()));
            } else {
                assert type.isRatifyStatus() : "Expected tribunal ratify niche/publication, not/" + type;
            }

        } else {
            assert type.isApproveSuggestedNiche() : "Encountered Unexpected referendumType/" + type + " r/" + referendum.getOid();
            getAreaContext().doAreaTask(new PutNicheOnSaleTask(niche, null));
        }
    }

    private void handleFailedReferendum(Referendum referendum) {
        ReferendumType type = referendum.getType();
        Niche niche = referendum.getNiche();

        if (type.isTribunalReferendum()) {
            // bl: set the status to null to indicate the appeal is closed
            referendum.getTribunalIssue().setStatus(null);

            if (type.isTribunalApproveNicheDetails()) {
                // jw: nothing to do for niche name/description edits that are rejected

            } else if (type.isTribunalRatifyPublication()) {
                Publication publication = referendum.getChannel().getConsumer();
                // bl: if the tribunal voted an active publication down, then reject it!
                getAreaContext().doAreaTask(new RejectPublicationTask(publication, referendum.getTribunalIssue()));

            } else {
                if (type.isTribunalRatifyNiche()) {
                    // bl: if the tribunal voted an active niche down, then reject it!
                    getAreaContext().doAreaTask(new RejectNicheTask(niche, referendum.getTribunalIssue()));
                } else {
                    // bl: in this case, the tribunal confirmed the niche's status as rejected, so nothing extra to do
                    assert type.isTribunalApproveRejectedNiche() : "Encountered unexpected tribunalReferendumType/" + type + " r/" + referendum.getOid();
                }

                // bl: once the Tribunal rejects a Niche, the decision is FINAL. release the reserved Niche name
                // so that it can be used again. this means that the now-rejected Niche will not be able to
                // be appealed ever again.
                niche.releaseReservedNameForPermanentlyRejectedNiche();
            }

        } else {
            assert type.isApproveSuggestedNiche() : "Encountered unexpected referendumType/" + type + " r/" + referendum.getOid();
            getAreaContext().doAreaTask(new RejectNicheTask(niche, null));
        }
    }
}
