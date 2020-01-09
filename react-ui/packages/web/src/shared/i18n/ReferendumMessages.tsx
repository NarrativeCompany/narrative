import { defineMessages } from 'react-intl';

export const ReferendumMessages = defineMessages({
  TribunalVoteDetails: {
    id: 'referendumVoteDetails.tribunalVoteDetails',
    defaultMessage: 'Tribunal Vote Details ({totalVotesText} Total Votes)'
  },
  VoteTally: {
    id: 'referendumVoteDetails.voteTally',
    defaultMessage: 'Vote Tally ({totalVotesText} {totalVotes, plural, one {Vote} other {Votes}},' +
      ' {pointTotalText} Total Points)'
  },
  ApprovalRating: {
    id: 'referendumVoteDetails.approvalRating',
    defaultMessage: 'Approval Rating'
  },
  VotedFor: {
    id: 'referendumVoteDetails.votedFor',
    defaultMessage: 'Voted for'
  },
  VotedAgainst: {
    id: 'referendumVoteDetails.votedAgainst',
    defaultMessage: 'Voted against'
  },
  TotalPoints: {
    id: 'referendumVoteDetails.totalPoints',
    defaultMessage: '{formattedPoints} Total Points'
  },
  Votes: {
    id: 'referendumVoteDetails.votes',
    defaultMessage: '{formattedPoints} Votes'
  },
  TribunalMembers: {
    id: 'referendumVoteDetails.tribunalMembers',
    defaultMessage: '{formattedPoints} Tribunal Members'
  },
  KeepNiche: {
    id: 'referendumVoteDetails.keepNiche',
    defaultMessage: 'Keep Niche'
  },
  RejectNiche: {
    id: 'referendumVoteDetails.rejectNiche',
    defaultMessage: 'Reject Niche'
  },
  VotedToApproveEdit: {
    id: 'referendumVoteDetails.votedToApproveEdit',
    defaultMessage: 'Voted to approve edit'
  },
  VotedToRejectEdit: {
    id: 'referendumVoteDetails.votedToRejectEdit',
    defaultMessage: 'Voted to reject edit'
  },
  YetToVote: {
    id: 'referendumVoteDetails.yetToVote',
    defaultMessage: 'Yet to vote'
  },
  ReasonForRejection: {
    id: 'referendumVoteReasonIcon.reasonForRejection',
    defaultMessage: 'Reason for Rejection'
  },
  RedundantReasonDescription: {
    id: 'referendumUtils.redundantReasonDescription',
    defaultMessage: 'User indicated that the Niche is redundant.'
  },
  ContainsProfanityReasonDescription: {
    id: 'referendumUtils.containsProfanityReasonDescription',
    defaultMessage: 'User indicated that the Niche contains profanity.'
  },
  ViolatesTosReasonDescription: {
    id: 'referendumUtils.violatesTosReasonDescription',
    defaultMessage: 'User indicated that the Niche violates the {termsOfService} or {acceptableUsePolicy}.'
  },
  SpellingIssueInNameReasonDescription: {
    id: 'referendumUtils.spellingIssueInNameReasonDescription',
    defaultMessage: 'User indicated that the Niche has a misspelling in the name.'
  },
  UnclearNameOrDescriptionReasonDescription: {
    id: 'referendumUtils.unclearNameOrDescriptionReasonDescription',
    defaultMessage: 'User indicated that the Niche is unclear, incorrect, or improperly defined.'
  },
  WrongLanguageReasonDescription: {
    id: 'referendumUtils.wrongLanguageReasonDescription',
    defaultMessage: 'User indicated that the Niche is not written in English.'
  },
  RedundantReasonRadio: {
    id: 'reviewCardBack.redundantReasonRadio',
    defaultMessage: 'Redundant/duplicate'
  },
  ContainsProfanityReasonRadio: {
    id: 'reviewCardBack.containsProfanityReasonRadio',
    defaultMessage: 'Contains profanity'
  },
  ViolatesTosReasonRadio: {
    id: 'reviewCardBack.violatesTosReasonRadio',
    defaultMessage: 'Violates the {termsOfService} or {acceptableUsePolicy}'
  },
  SpellingIssueInNameReasonRadio: {
    id: 'reviewCardBack.spellingIssueInNameReasonRadio',
    defaultMessage: 'Contains misspelling'
  },
  UnclearNameOrDescriptionReasonRadio: {
    id: 'reviewCardBack.unclearNameOrDescriptionReasonRadio',
    defaultMessage: 'Unclear, incorrect, or not properly defined'
  },
  WrongLanguageReasonRadio: {
    id: 'reviewCardBack.wrongLanguageReasonRadio',
    defaultMessage: 'Not in English'
  },
  UniqueAssertion: {
    id: 'referendumMessages.uniqueAssertion',
    defaultMessage: 'is unique'
  },
  ClearlyDefinedAssertion: {
    id: 'referendumMessages.clearlyDefinedAssertion',
    defaultMessage: 'is clearly and properly defined'
  },
  LanguageAssertion: {
    id: 'nicheConfirmationStep.languageAssertion',
    defaultMessage: 'is in English'
  },
  SpelledProperlyAssertion: {
    id: 'referendumMessages.spelledProperlyAssertion',
    defaultMessage: 'is spelled properly'
  },
  ContainsNoProfanityAssertion: {
    id: 'referendumMessages.containsNoProfanityAssertion',
    defaultMessage: 'does not contain profanity'
  },
  CompliesWithTosAssertion: {
    id: 'referendumMessages.compliesWithTosAssertion',
    defaultMessage: 'My suggestion does not violate the {termsOfService} or {acceptableUsePolicy}.'
  },
  TOS: {
    id: 'referendumMessages.tos',
    defaultMessage: 'TOS'
  },
  AUP: {
    id: 'referendumMessages.aup',
    defaultMessage: 'AUP'
  },
  Keep: {
    id: 'referendumType.keep',
    defaultMessage: 'keep'
  },
  Approve: {
    id: 'referendumType.approve',
    defaultMessage: 'approve'
  },
  Reject: {
    id: 'referendumType.reject',
    defaultMessage: 'reject'
  },
  YouVotedToMessageForEdit: {
    id: 'referendumType.youVotedToMessageForEdit',
    defaultMessage: 'You voted to {actionText} the edit request.'
  },
  YouVotedToNicheMessage: {
    id: 'referendumType.youVotedToNicheMessage',
    defaultMessage: 'You voted to {actionText} this Niche!'
  },
  YouVotedToPublicationMessage: {
    id: 'referendumType.youVotedToPublicationMessage',
    defaultMessage: 'You voted to {actionText} this Publication!'
  },
  YouDidNotVoteText: {
    id: 'referendumCurrentVoteDescription.youDidNotVoteText',
    defaultMessage: 'You did not cast your vote.'
  },
  YouNeedToVoteText: {
    id: 'referendumCurrentVoteDescription.youNeedToVoteText',
    defaultMessage: 'You have not cast your vote.'
  },
  ApproveSuggestedNiche: {
    id: 'referendumType.approveSuggestedNiche',
    defaultMessage: 'Initial Approval'
  },
  RatifyNiche: {
    id: 'referendumType.ratifyNiche',
    defaultMessage: 'Niche Status'
  },
  ApproveRejectedNiche: {
    id: 'referendumType.approveRejectedNiche',
    defaultMessage: 'Niche Status'
  },
  TribunalApproveNicheDetailChange: {
    id: 'referendumType.tribunalApprovalNicheDetailChange',
    defaultMessage: 'Niche Details Edit'
  },
  TribunalApproveRejectedNiche: {
    id: 'referendumSummarySection.tribunalApproveRejectedNiche',
    defaultMessage: 'Niche Status'
  },
  TribunalRatifyPublication: {
    id: 'referendumType.tribunalRatifyPublication',
    defaultMessage: 'Publication Status'
  },
  TribunalRatifyNiche: {
    id: 'referendumType.tribunalRatifyNiche',
    defaultMessage: 'Niche Status'
  },
  Summary: {
    id: 'referendumSummarySection.summary',
    defaultMessage: 'Summary'
  },
  SummaryType: {
    id: 'referendumSummarySection.type',
    defaultMessage: 'Type'
  },
  CurrentStatus: {
    id: 'approvalSummary.currentStatus',
    defaultMessage: 'Current Status'
  },
  Suggester: {
    id: 'referendumSummarySection.suggester',
    defaultMessage: 'Suggester'
  },
  ApprovalPeriodStart: {
    id: 'referendumSummarySection.approvalPeriodStart',
    defaultMessage: 'Approval Period Start'
  },
  ApprovalPeriodEnd: {
    id: 'referendumSummarySection.approvalPeriodEnd',
    defaultMessage: 'Approval Period End'
  },
  AppealPeriodStart: {
    id: 'referendumSummarySection.appealPeriodStart',
    defaultMessage: 'Appeal Period Start'
  },
  AppealPeriodEnd: {
    id: 'referendumSummarySection.appealPeriodEnd',
    defaultMessage: 'Appeal Period End'
  },
  TotalVotes: {
    id: 'referendumSummarySection.totalVotes',
    defaultMessage: 'Total Votes'
  },
  TotalVotePoints: {
    id: 'referendumSummarySection.totalVotePoints',
    defaultMessage: 'Total Vote Points'
  },
  UpVotePercentage: {
    id: 'referendumSummarySection.upVotePercentage',
    defaultMessage: 'Up Vote %'
  },
  FinalResult: {
    id: 'referendumSummarySection.finalResult',
    defaultMessage: 'Final Result'
  },
  UnderReview: {
    id: 'referendumSummarySection.underReview',
    defaultMessage: 'Under Review'
  },
  ApprovalEnded: {
    id: 'referendumSummarySection.approvalEnded',
    defaultMessage: 'Approval Ended'
  },
  AppealEnded: {
    id: 'referendumSummarySection.appealEnded',
    defaultMessage: 'Appeal Ended'
  },
  NicheApproved: {
    id: 'referendumType.nicheApproved',
    defaultMessage: 'Niche Approved'
  },
  NicheDetailsApproved: {
    id: 'referendumType.nicheDetailsApproved',
    defaultMessage: 'Niche Details Approved'
  },
  NicheRejected: {
    id: 'referendumType.nicheRejected',
    defaultMessage: 'Niche Rejected'
  },
  NicheDetailsRejected: {
    id: 'referendumType.nicheDetailsRejected',
    defaultMessage: 'Niche Details Rejected'
  },
  NicheStatusUnchanged: {
    id: 'referendumType.nicheStatusUnchanged',
    defaultMessage: 'Niche Status Unchanged'
  },
  PublicationStatusUnchanged: {
    id: 'referendumType.publicationStatusUnchanged',
    defaultMessage: 'Publication Status Unchanged'
  },
  PublicationRejected: {
    id: 'referendumType.publicationRejected',
    defaultMessage: 'Publication Rejected'
  },
  SuggestedNichePassed: {
    id: 'referendumType.suggestedNichePassed',
    defaultMessage: 'The Niche was approved with a {percentage} up-vote.'
  },
  SuggestedNicheNotPassed: {
    id: 'referendumType.suggestedNicheNotPassed',
    defaultMessage: 'The Niche was rejected with a {percentage} down-vote.'
  },
  NicheRatificationPassed: {
    id: 'referendumType.nicheRatificationPassed',
    defaultMessage: 'The Niche was approved with a {percentage} up-vote. Because the vote affirmed the current ' +
      'status, the Niche status remained APPROVED.'
  },
  NicheRatificationNotPassed: {
    id: 'referendumType.nicheRatificationNotPassed',
    defaultMessage: 'The Niche was rejected with a {percentage} down-vote. The Niche status was changed to REJECTED.'
  },
  PublicationRatificationPassed: {
    id: 'referendumType.publicationRatificationPassed',
    defaultMessage: 'The Publication was approved with a {percentage} up-vote. Because the vote affirmed the current ' +
      'status, the Publication status remained APPROVED.'
  },
  PublicationRatificationNotPassed: {
    id: 'referendumType.publicationRatificationNotPassed',
    defaultMessage: 'The Publication was rejected with a {percentage} down-vote. The Publication was removed.'
  },
  ApproveRejectedNichePassed: {
    id: 'referendumType.approveRejectedNichePassed',
    defaultMessage: 'The Niche was approved with a {percentage} up-vote. The Niche status was changed to APPROVED.'
  },
  ApproveRejectedNicheNotPassed: {
    id: 'referendumType.approveRejectedNicheNotPassed',
    defaultMessage: 'The Niche was rejected with a {percentage} down-vote. Because the vote affirmed the current ' +
      'status, the Niche status remained REJECTED.'
  },
  NicheDetailsChangePassed: {
    id: 'referendumType.nicheDetailsChangePassed',
    defaultMessage: 'The Niche details edit was approved with a {percentage} up-vote.'
  },
  NicheDetailsChangeNotPassed: {
    id: 'referendumType.nicheDetailsChangeNotPassed',
    defaultMessage: 'The Niche details edit was rejected with a {percentage} down-vote.'
  },
});
