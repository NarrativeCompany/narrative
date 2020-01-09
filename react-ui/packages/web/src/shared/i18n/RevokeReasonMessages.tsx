import { defineMessages } from 'react-intl';

export const RevokeReasonMessages = defineMessages({
  SuggestNiche24Hours: {
    id: 'permissionsMessages.suggestNiche24Hours',
    defaultMessage: 'You already suggested a Niche today!'
  },
  AppealedInLast24Hours: {
    id: 'permissionsMessages.appealedInLast24Hours',
    defaultMessage: 'You already appealed a Niche today!'
  },
  NicheSlotsFull: {
    id: 'permissionsMessages.nicheSlotsFull',
    defaultMessage: 'You have already filled all of your Niche slots!'
  },
  SecurityDepositRequired: {
    id: 'permissionsMessages.securityDepositRequired',
    defaultMessage: 'You cannot bid because you are not Medium Reputation (50+) or higher!'
  },
  SuggestNicheConductNegative: {
    id: 'permissionsMessages.suggestNicheConductNegative',
    defaultMessage: 'You are Conduct Negative, so you cannot suggest a Niche right now!'
  },
  ConductRevoke: {
    id: 'permissionsMessages.conductRevoke',
    defaultMessage: 'You cannot {attemptedAction} because you are Conduct Negative!'
  },
  ConductNegativeGetCertified: {
    id: 'permissionsMessages.conductNegativeGetCertified',
    defaultMessage: 'You can end your Conduct Negative status immediately by getting Certified.'
  },
  LowReputationGetCertified: {
    id: 'permissionsMessages.lowReputationGetCertified',
    defaultMessage: 'You can boost your reputation by 30 points immediately by getting Certified.'
  },
  NotCertified: {
    id: 'permissionsMessages.notCertified',
    defaultMessage: 'You cannot do this because you are not Certified!'
  },
  LowReputation: {
    id: 'permissionsMessages.lowReputation',
    defaultMessage: 'You cannot do this because you are not Medium Reputation (50+) or higher!'
  },
  VoteOnApprovals: {
    id: 'permissionsMessages.voteOnApprovals',
    defaultMessage: 'vote on approvals'
  },
  EditPosts: {
    id: 'permissionsMessages.editPosts',
    defaultMessage: 'edit posts'
  },
  BidOnNiches: {
    id: 'permissionsMessages.bidOnNiches',
    defaultMessage: 'bid on Niches'
  },
  PostComments: {
    id: 'permissionsMessages.postComments',
    defaultMessage: 'post comments'
  },
  SuggestNiche: {
    id: 'permissionsMessages.suggestNiche',
    defaultMessage: 'suggest a Niche'
  },
  SuggestNicheTimeout: {
    id: 'permissionsMessages.suggestNicheTimeout',
    defaultMessage: 'You are welcome to make another suggestion in: {restorationCountdown}'
  },
  CreatePublication: {
    id: 'permissionsMessages.createPublication',
    defaultMessage: 'create a Publication'
  },
  PostContent: {
    id: 'permissionsMessages.postContent',
    defaultMessage: 'post content'
  },
  NominateForModeratorElection: {
    id: 'permissionsMessages.nominateForModeratorElection',
    defaultMessage: 'nominate yourself'
  },
  SubmitTribunalAppeal: {
    id: 'permissionsMessages.submitTribunalAppeal',
    defaultMessage: 'appeal this Niche'
  },
  SubmitTribunalAppealTimeout: {
    id: 'permissionsMessages.submitTribunalAppealTimeout',
    defaultMessage: 'You are welcome to appeal this Niche in: {restorationCountdown}'
  },
  DefaultAction: {
    id: 'permissionsMessages.defaultAction',
    defaultMessage: 'perform this action'
  },
  SignIn: {
    id: 'permissionsMessages.signIn',
    defaultMessage: 'sign in'
  },
  MustSignInToPerformAction: {
    id: 'permissionsMessages.mustSignInToPerformAction',
    defaultMessage: 'You must {signIn} to {performAction}.'
  },
});
