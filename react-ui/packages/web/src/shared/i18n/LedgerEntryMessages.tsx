import { defineMessages } from 'react-intl';

export const LedgerEntryMessages = defineMessages({
  NoEntriesInHistory: {
    id: 'ledgerEntries.noEntriesInHistory',
    defaultMessage: 'There is currently nothing in this history!'
  },
  ViewNiche: {
    id: 'ledgerEntries.viewNiche',
    defaultMessage: 'View Niche'
  },
  ViewElection: {
    id: 'ledgerEntries.viewElection',
    defaultMessage: 'View Election'
  },
  ViewAuction: {
    id: 'ledgerEntries.viewAuction',
    defaultMessage: 'View Auction'
  },
  ViewAppeal: {
    id: 'ledgerEntries.viewAppeal',
    defaultMessage: 'View Appeal'
  },
  ViewApproval: {
    id: 'ledgerEntries.viewApproval',
    defaultMessage: 'View Approval'
  },
  ViewReputation: {
    id: 'ledgerEntries.viewReputation',
    defaultMessage: 'View Reputation'
  },
  ViewPost: {
    id: 'ledgerEntries.viewPost',
    defaultMessage: 'View Post'
  },
  Approved: {
    id: 'ledgerEntries.approved',
    defaultMessage: 'approved'
  },
  Rejected: {
    id: 'ledgerEntries.declined',
    defaultMessage: 'rejected'
  },
  Approval: {
    id: 'ledgerEntries.approval',
    defaultMessage: 'approval'
  },
  Rejection: {
    id: 'ledgerEntries.rejection',
    defaultMessage: 'rejection'
  },
  Approve: {
    id: 'ledgerEntries.approve',
    defaultMessage: 'approve'
  },
  Reject: {
    id: 'ledgerEntries.reject',
    defaultMessage: 'reject'
  },
  ReVote: {
    id: 'ledgerEntries.reVote',
    defaultMessage: 're-vote'
  },
  ApproveWithNicheLink: {
    id: 'ledgerEntries.approveWithNicheLink',
    defaultMessage: 'approve {nicheLink}'
  },
  KeepRejectedWithNicheLink: {
    id: 'ledgerEntries.keepRejectedWithNicheLink',
    defaultMessage: 'keep {nicheLink} rejected'
  },
  RejectWithChannelLink: {
    id: 'ledgerEntries.rejectWithChannelLink',
    defaultMessage: 'reject {channelLink}'
  },
  KeepActiveWithChannelLink: {
    id: 'ledgerEntries.keepActiveWithChannelLink',
    defaultMessage: 'keep {channelLink} active'
  },
  DeletedPost: {
    id: 'ledgerEntries.deletedPost',
    defaultMessage: 'a deleted Post'
  },
  // FIXME: This breaks translate:extract #921
  // jw: The keys here need to be a bit funky so that we can look them up "magically". Place any normal messages
  //     above here, since it's gonna get crazy from here down.
  // tslint:disable max-line-length
  'titleForChannel.NICHE_SUGGESTED': {
    id: 'ledgerEntryTitle.nicheSuggested',
    defaultMessage: '{actorLink} suggested this Niche.'
  },
  'titleForChannel.NICHE_REFERENDUM_RESULT': {
    id: 'ledgerEntryTitle.nicheRefendumResult',
    defaultMessage: 'The community {approvedOrRejectedFromReferendum} this Niche with {referendumVotePercentage}% {approvalOrRejection}.'
  },
  'titleForChannel.NICHE_AUCTION_STARTED': {
    id: 'ledgerEntryTitle.nicheAuctionStarted',
    defaultMessage: 'The auction started for this Niche.'
  },
  'titleForChannel.NICHE_AUCTION_RESTARTED': {
    id: 'ledgerEntryTitle.nicheAuctionRestarted',
    defaultMessage: 'Due to the Niche not being paid for, the auction was restarted.'
  },
  'titleForChannel.NICHE_AUCTION_ENDED': {
    id: 'ledgerEntryTitle.nicheAuctionEnded',
    defaultMessage: 'The auction for this Niche ended with {bidCount, number} {bidCount, plural, one {bid} other {bids}} and a winning bid of {bidNrveValue}.'
  },
  'titleForChannel.NICHE_AUCTION_WON': {
    id: 'ledgerEntryTitle.nicheAuctionWon',
    defaultMessage: '{bidderLink} won the auction for {bidNrveValue}.'
  },
  'titleForChannel.NICHE_OWNER_REMOVED': {
    id: 'ledgerEntryTitle.nicheOwnerRemoved',
    defaultMessage: '{actorLink} was removed as the owner of this niche.'
  },
  'titleForChannel.NICHE_AUCTION_FALLBACK_WON': {
    id: 'ledgerEntryTitle.nicheAuctionFallbackWon',
    defaultMessage: 'Because the previous winner failed to pay, {bidderLink} became the new winner with a bid of {bidNrveValue}.'
  },
  'titleForChannel.NICHE_INVOICE_PAID': {
    id: 'ledgerEntryTitle.nicheInvoicePaid',
    defaultMessage: '{bidderLink} paid {bidNrveValue} for {nicheLink}.'
  },
  'titleForChannel.NICHE_INVOICE_FAILED': {
    id: 'ledgerEntryTitle.nicheInvoiceFailed',
    defaultMessage: '{actorLink} failed to make the required payment of {bidNrveValue} for this Niche.'
  },
  'titleForChannel.NICHE_INVOICE_FAILED.withSecurityDeposit': {
    id: 'ledgerEntryTitle.nicheInvoiceFailed.withSecurityDeposit',
    defaultMessage: '{actorLink} failed to make the required payment of {bidNrveValue} for this Niche and lost the {securityDepositValue} security deposit.'
  },
  'titleForChannel.ISSUE_REPORT.TRIBUNAL_RATIFY_NICHE': {
    id: 'ledgerEntryTitle.issueReferendumStart.ratifyNiche',
    defaultMessage: '{actorLink} appealed for rejection.'
  },
  'titleForChannel.ISSUE_REPORT.TRIBUNAL_RATIFY_PUBLICATION': {
    id: 'ledgerEntryTitle.issueReferendumStart.ratifyPublication',
    defaultMessage: '{actorLink} appealed for rejection.'
  },
  'titleForChannel.ISSUE_REPORT.TRIBUNAL_APPROVE_REJECTED_NICHE': {
    id: 'ledgerEntryTitle.issueReferendumStart.approveRejectedNiche',
    defaultMessage: '{actorLink} appealed for approval.'
  },
  'titleForChannel.NICHE_EDIT': {
    id: 'ledgerEntryTitle.issueReferendumStart.detailsChange',
    defaultMessage: '{actorLink} submitted an edit request to the Tribunal'
  },
  'titleForChannel.ISSUE_REFERENDUM_RESULT.TRIBUNAL_APPROVE_NICHE_DETAIL_CHANGE.affirmed': {
    id: 'ledgerEntryTitle.issueReferendumResult.nicheDetailsChange.affirmed',
    defaultMessage: 'The Tribunal approved the Niche owner’s edit request.'
  },
  'titleForChannel.ISSUE_REFERENDUM_RESULT.TRIBUNAL_APPROVE_NICHE_DETAIL_CHANGE.notAffirmed': {
    id: 'ledgerEntryTitle.issueReferendumResult.nicheStatusChange.notAffirmed',
    defaultMessage: 'The Tribunal rejected the Niche owner’s edit request.'
  },
  'titleForChannel.ISSUE_REFERENDUM_RESULT.TRIBUNAL_APPROVE_REJECTED_NICHE.affirmed': {
    id: 'ledgerEntryTitle.issueReferendumResult.tribunalApproveRejectedNiche.affirmed',
    defaultMessage: 'The Tribunal approved the previously-rejected Niche.'
  },
  'titleForChannel.ISSUE_REFERENDUM_RESULT.TRIBUNAL_APPROVE_REJECTED_NICHE.notAffirmed': {
    id: 'ledgerEntryTitle.issueReferendumResult.tribunalApproveRejectedNiche.notAffirmed',
    defaultMessage: 'The Tribunal affirmed the Niche’s status as REJECTED. Thus, no further action was required.'
  },
  'titleForChannel.ISSUE_REFERENDUM_RESULT.TRIBUNAL_RATIFY_NICHE.affirmed': {
    id: 'ledgerEntryTitle.issueReferendumResult.ratifyNiche.affirmed',
    defaultMessage: 'The Tribunal affirmed the Niche’s status as APPROVED. Thus, no further action was required.'
  },
  'titleForChannel.ISSUE_REFERENDUM_RESULT.TRIBUNAL_RATIFY_NICHE.notAffirmed': {
    id: 'ledgerEntryTitle.issueReferendumResult.ratifyNiche.notAffirmed',
    defaultMessage: 'The Tribunal rejected the Niche.'
  },
  'titleForChannel.ISSUE_REFERENDUM_RESULT.TRIBUNAL_RATIFY_PUBLICATION.affirmed': {
    id: 'ledgerEntryTitle.issueReferendumResult.ratifyPublication.affirmed',
    defaultMessage: 'The Tribunal affirmed the Publication’s status as APPROVED. Thus, no further action was required.'
  },
  'titleForChannel.ISSUE_REFERENDUM_RESULT.TRIBUNAL_RATIFY_PUBLICATION.notAffirmed': {
    id: 'ledgerEntryTitle.issueReferendumResult.ratifyPublication.notAffirmed',
    defaultMessage: 'The Tribunal rejected the Publication.'
  },
  'titleForChannel.NICHE_MODERATOR_NOMINATING_STARTED': {
    id: 'ledgerEntryTitle.nicheModeratorNominatingStarted',
    defaultMessage: 'Niche moderator election started.'
  },
  'titleForChannel.NICHE_MODERATOR_NOMINATED': {
    id: 'ledgerEntryTitle.nicheModeratorNominated',
    defaultMessage: '{actorLink} added as Niche Moderator nominee.'
  },
  'titleForChannel.NICHE_MODERATOR_NOMINEE_WITHDRAWN': {
    id: 'ledgerEntryTitle.nicheModeratorWithdrawn',
    defaultMessage: '{actorLink} withdrawn as Niche Moderator nominee.'
  },
  'titleForChannel.POST_REMOVED_FROM_CHANNEL': {
    id: 'ledgerEntryTitle.postRemovedFromNiche',
    defaultMessage: '{actorLink} removed {postLink}.'
  },
  'titleForChannel.POST_REMOVED_FROM_CHANNEL.withDeletedPost': {
    id: 'ledgerEntryTitle.postRemovedFromNiche.withDeletedPost',
    defaultMessage: '{actorLink} removed a post.'
  },
  'titleForChannel.PUBLICATION_CREATED': {
    id: 'ledgerEntryTitle.publicationCreated',
    defaultMessage: '{actorLink} created this Publication.'
  },
  'titleForChannel.PUBLICATION_PAYMENT.INITIAL': {
    id: 'ledgerEntryTitle.publicationPayment.initial',
    defaultMessage: '{actorLink} activated the {publicationPlanName} plan.'
  },
  'titleForChannel.PUBLICATION_PAYMENT.RENEWAL': {
    id: 'ledgerEntryTitle.publicationPayment.renewal',
    defaultMessage: '{actorLink} renewed this Publication on the {publicationPlanName} plan.'
  },
  'titleForChannel.PUBLICATION_PAYMENT.UPGRADE': {
    id: 'ledgerEntryTitle.publicationPayment.upgrade',
    defaultMessage: '{actorLink} upgraded this Publication to the {publicationPlanName} plan.'
  },
  'titleForChannel.PUBLICATION_EDITOR_DELETED_COMMENT.withCommentOid': {
    id: 'ledgerEntryTitle.publicationEditorDeletedComment',
    defaultMessage: '{actorLink} removed a comment by {authorLink} from {postLink}.'
  },
  'titleForProfile.NICHE_REFERENDUM_VOTE.APPROVE_SUGGESTED_NICHE': {
    id: 'ledgerEntryTitle.forProfile.nicheReferendumVote.approveSuggestedNiche',
    defaultMessage: 'Voted to {approveOrRejectFromVote} {nicheLink}.'
  },
  'titleForProfile.NICHE_REFERENDUM_VOTE.APPROVE_REJECTED_NICHE': {
    id: 'ledgerEntryTitle.forProfile.nicheReferendumVote.approveRejectedNiche',
    defaultMessage: 'Voted to {approveOrKeepRejectedWithNicheLink}.'
  },
  'titleForProfile.NICHE_REFERENDUM_VOTE.RATIFY_NICHE': {
    id: 'ledgerEntryTitle.forProfile.nicheReferendumVote.ratifyNiche',
    defaultMessage: 'Voted to {keepActiveOrRejectWithNicheLink}.'
  },
  'titleForProfile.ISSUE_REFERENDUM_VOTE.TRIBUNAL_APPROVE_REJECTED_NICHE': {
    id: 'ledgerEntryTitle.forProfile.issueReferendumVote.approveRejectedNiche',
    defaultMessage: 'Voted to {approveOrKeepRejectedWithNicheLink}.'
  },
  'titleForProfile.ISSUE_REFERENDUM_VOTE.TRIBUNAL_RATIFY_NICHE': {
    id: 'ledgerEntryTitle.forProfile.issueReferendumVote.ratifyNiche',
    defaultMessage: 'Voted to {keepActiveOrRejectWithNicheLink}.'
  },
  'titleForProfile.ISSUE_REFERENDUM_VOTE.TRIBUNAL_RATIFY_PUBLICATION': {
    id: 'ledgerEntryTitle.forProfile.issueReferendumVote.ratifyPublication',
    defaultMessage: 'Voted to {keepActiveOrRejectWithPublicationLink}.'
  },
  'titleForProfile.ISSUE_REFERENDUM_VOTE.TRIBUNAL_APPROVE_NICHE_DETAIL_CHANGE': {
    id: 'ledgerEntryTitle.forProfile.issueReferendumVote.approveNicheDetailChange',
    defaultMessage: 'Voted to {approveOrRejectFromVote} the detail change for {nicheLink}.'
  },
  'titleForProfile.ISSUE_REPORT.TRIBUNAL_APPROVE_REJECTED_NICHE': {
    id: 'ledgerEntryTitle.forProfile.issueReport.approveRejectedNiche',
    defaultMessage: 'Reported {nicheLink} to the Tribunal to consider approving it.'
  },
  'titleForProfile.ISSUE_REPORT.TRIBUNAL_RATIFY_NICHE': {
    id: 'ledgerEntryTitle.forProfile.issueReport.ratifyNiche',
    defaultMessage: 'Reported {nicheLink} to the Tribunal to consider whether it should remain live.'
  },
  'titleForProfile.ISSUE_REPORT.TRIBUNAL_RATIFY_PUBLICATION': {
    id: 'ledgerEntryTitle.forProfile.issueReport.ratifyPublication',
    defaultMessage: 'Reported {publicationLink} to the Tribunal to consider whether it should remain live.'
  },
  'titleForProfile.NICHE_EDIT': {
    id: 'ledgerEntryTitle.forProfile.detailsChange',
    defaultMessage: 'Submitted appeal to change the details of {nicheLink}.'
  },
  'titleForProfile.NICHE_SUGGESTED': {
    id: 'ledgerEntryTitle.forProfile.nicheSuggested',
    defaultMessage: 'Suggested {nicheLink}.'
  },
  'titleForProfile.NICHE_BID': {
    id: 'ledgerEntryTitle.forProfile.nicheBid',
    defaultMessage: 'Placed a bid of {bidNrveValue} for {nicheLink}.'
  },
  'titleForProfile.NICHE_INVOICE_PAID': {
    id: 'ledgerEntryTitle.forProfile.nicheInvoicePaid',
    defaultMessage: 'Paid {bidNrveValue} for {nicheLink}.'
  },
  'titleForProfile.NICHE_INVOICE_FAILED': {
    id: 'ledgerEntryTitle.forProfile.nicheInvoiceFailed',
    defaultMessage: 'Failed to pay {bidNrveValue} for {nicheLink} by the payment due date.'
  },
  'titleForProfile.NICHE_INVOICE_FAILED.withSecurityDeposit': {
    id: 'ledgerEntryTitle.forProfile.nicheInvoiceFailed.withSecurityDeposit',
    defaultMessage: 'Failed to pay {bidNrveValue} for {nicheLink} by the payment due date and lost the {securityDepositValue} security deposit.'
  },
  'titleForProfile.NICHE_AUCTION_WON': {
    id: 'ledgerEntryTitle.forProfile.nicheInvoiceWon',
    defaultMessage: 'Won the auction for {nicheLink} with a winning bid of {bidNrveValue}.'
  },
  'titleForProfile.NICHE_OWNER_REMOVED': {
    id: 'ledgerEntryTitle.forProfile.nicheOwnerRemoved',
    defaultMessage: 'Removed as owner of {nicheLink}.'
  },
  'titleForProfile.PAYMENT_CHARGEBACK.NICHE_AUCTION': {
    id: 'ledgerEntryTitle.forProfile.paymentChargeback.NICHE_AUCTION',
    defaultMessage: 'Disputed transaction (chargeback) for the Niche purchase transaction for {nicheLink}.'
  },
  'titleForProfile.PAYMENT_CHARGEBACK.KYC_CERTIFICATION': {
    id: 'ledgerEntryTitle.forProfile.paymentChargeback.KYC_CERTIFICATION',
    defaultMessage: 'Disputed transaction (chargeback) for Certification.'
  },
  'titleForProfile.NICHE_AUCTION_FALLBACK_WON': {
    id: 'ledgerEntryTitle.forProfile.nicheInvoiceFallbackWon',
    defaultMessage: 'Became the winner of {nicheLink} with a winning bid of {bidNrveValue} because the previous winner failed to pay.'
  },
  'titleForProfile.NICHE_MODERATOR_NOMINATED': {
    id: 'ledgerEntryTitle.forProfile.nicheModeratorNominated',
    defaultMessage: 'Nominated to moderate {nicheLink}.'
  },
  'titleForProfile.NICHE_MODERATOR_NOMINEE_WITHDRAWN': {
    id: 'ledgerEntryTitle.forProfile.nicheModeratorWithdrawn',
    defaultMessage: 'Withdrew nomination to moderate {nicheLink}.'
  },
  'titleForProfile.KYC_CERTIFICATION_APPROVED': {
    id: 'ledgerEntryTitle.forProfile.kycCertificationApproved',
    defaultMessage: 'Became Certified.'
  },
  'titleForProfile.KYC_CERTIFICATION_REVOKED': {
    id: 'ledgerEntryTitle.forProfile.kycCertificationRevoked',
    defaultMessage: 'Certification revoked due to chargeback.'
  },
  'titleForProfile.KYC_REFUND': {
    id: 'ledgerEntryTitle.forProfile.kycRefund',
    defaultMessage: 'Certification fee was refunded.'
  },
  'titleForProfile.POST_REMOVED_FROM_CHANNEL': {
    id: 'ledgerEntryTitle.forProfile.postRemovedFromNiche',
    defaultMessage: 'Removed {postLink} from {channelLink}.'
  },
  'titleForProfile.POST_REMOVED_FROM_CHANNEL.withDeletedPost': {
    id: 'ledgerEntryTitle.forProfile.postRemovedFromNiche.withDeletedPost',
    defaultMessage: 'Removed a post from {channelLink}.'
  },
  'titleForProfile.USER_PUBLISHED_POST': {
    id: 'ledgerEntryTitle.forProfile.userPublishedPost',
    defaultMessage: 'Published {postLink}.'
  },
  'titleForProfile.USER_PUBLISHED_POST.withDeletedPost': {
    id: 'ledgerEntryTitle.forProfile.userPublishedPost.withDeletedPost',
    defaultMessage: 'Published a post.'
  },
  'titleForProfile.USER_DELETED_POST': {
    id: 'ledgerEntryTitle.forProfile.userdeletedPost',
    defaultMessage: 'Deleted a post.'
  },
  'titleForProfile.TRIBUNAL_USER_DELETED_POST_OR_COMMENT_AUP_VIOLATION': {
    id: 'ledgerEntryTitle.forProfile.tribunalMemberDeletedPostOrCommentForAupViolation',
    defaultMessage: 'Removed a post by {authorLink} for an {aupLink} violation.'
  },
  'titleForProfile.TRIBUNAL_USER_DELETED_POST_OR_COMMENT_AUP_VIOLATION.withCommentOid': {
    id: 'ledgerEntryTitle.forProfile.tribunalMemberDeletedPostOrCommentForAupViolation.withCommentOid',
    defaultMessage: 'Removed a comment by {authorLink} for an {aupLink} violation.'
  },
  'titleForProfile.USER_HAD_POST_OR_COMMENT_DELETED_FOR_AUP_VIOLATION': {
    id: 'ledgerEntryTitle.forProfile.userHadPostOrCommentDeletedForAupViolation',
    defaultMessage: 'Had post removed due to an {aupLink} violation.'
  },
  'titleForProfile.USER_HAD_POST_OR_COMMENT_DELETED_FOR_AUP_VIOLATION.withCommentOid': {
    id: 'ledgerEntryTitle.forProfile.userHadPostOrCommentDeletedForAupViolation.withCommentOid',
    defaultMessage: 'Had comment removed due to an {aupLink} violation.'
  },
  'titleForProfile.PUBLICATION_CREATED': {
    id: 'ledgerEntryTitle.forProfile.publicationCreated',
    defaultMessage: 'Created Publication {publicationLink}.'
  },
  'titleForProfile.PUBLICATION_PAYMENT.INITIAL': {
    id: 'ledgerEntryTitle.forProfile.publicationPayment.initial',
    defaultMessage: 'Activated Publication {publicationLink} on the {publicationPlanName} plan.'
  },
  'titleForProfile.PUBLICATION_PAYMENT.RENEWAL': {
    id: 'ledgerEntryTitle.forProfile.publicationPayment.renewal',
    defaultMessage: 'Renewed Publication {publicationLink} on the {publicationPlanName} plan.'
  },
  'titleForProfile.PUBLICATION_PAYMENT.UPGRADE': {
    id: 'ledgerEntryTitle.forProfile.publicationPayment.upgrade',
    defaultMessage: 'Upgraded Publication {publicationLink} to the {publicationPlanName} plan.'
  },
  'titleForProfile.PUBLICATION_EDITOR_DELETED_COMMENT.withCommentOid': {
    id: 'ledgerEntryTitle.forProfile.publicationEditorDeletedComment',
    defaultMessage: 'Removed a comment by {authorLink} from {postLink} on {publicationLink}.'
  },
  'titleForProfile.USER_HAD_COMMENT_DELETED_BY_PUBLICATION_EDITOR.withCommentOid': {
    id: 'ledgerEntryTitle.forProfile.userHadCommentDeletedByPublicationEditor',
    defaultMessage: 'Had comment removed from {postLink} on {publicationLink}.'
  },
  // tslint:enable max-line-length
});
