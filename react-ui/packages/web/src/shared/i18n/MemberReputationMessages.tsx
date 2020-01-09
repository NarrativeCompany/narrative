import { defineMessages } from 'react-intl';

const HIGH_REP_THRESHOLD = 85;

export const MemberReputationMessages = defineMessages({
  PageHeaderTitle: {
    id: 'memberReputationMessages.pageHeaderTitle',
    defaultMessage: '{displayName} - Reputation'
  },
  PageHeaderDescription: {
    id: 'memberReputationMessages.pageHeaderDescription',
    defaultMessage: 'Reputation determines the impact of all actions. The actions taken by someone with higher' +
      ' reputation will be more influential than someone with lower reputation.'
  },
  ReputationScoreSectionTitleForCurrentUser: {
    id: 'memberReputationMessages.reputationScoreSectionTitleForCurrentUser',
    defaultMessage: 'Your Reputation Score'
  },
  ReputationScoreSectionTitle: {
    id: 'memberReputationMessages.reputationScoreSectionTitle',
    defaultMessage: 'Reputation Score'
  },

  ConductNegativeReputationTitle: {
    id: 'memberReputationMessages.conductNegativeReputationTitle',
    defaultMessage: 'Conduct Negative'
  },
  ConductNegativeReputationGetKYCMessage: {
    id: 'memberReputationMessages.conductNegativeReputationGetKycMessage',
    defaultMessage: 'Important: You may reverse your Conduct Negative status immediately by being {certifiedLink}. ' +
      'Once {certifiedLink}, your penalty time will expire automatically.'
  },
  ConductNegativePenaltyTimeExpires: {
    id: 'memberReputationMessages.conductNegativePenaltyTimeExpires',
    defaultMessage: 'Penalty Time Expires: {expiration}'
  },
  ConductNegativeReputationMessageForCurrentUser: {
    id: 'memberReputationMessages.conductNegativeReputationMessageForCurrentUser',
    defaultMessage: 'You are being penalized for certain actions you have taken. While Conduct Negative, you will be ' +
      'restricted from performing all of the following, until your penalty time elapses: a) suggesting, voting on, ' +
      'bidding on, or appealing Niches, b) being nominated for elections, c) posting content or comments.'
  },
  ConductNegativeReputationMessage: {
    id: 'memberReputationMessages.conductNegativeReputationMessage',
    defaultMessage: 'This member is being penalized for certain actions they have taken. While Conduct Negative, ' +
      'they are restricted from performing all of the following, until the penalty time elapses: a) suggesting, ' +
      'voting on, bidding on, or appealing Niches, b) being nominated for elections, c) posting content or comments.'
  },
  LowReputationTitle: {
    id: 'memberReputationMessages.lowReputationTitle',
    defaultMessage: 'Low Reputation'
  },
  MediumReputationTitle: {
    id: 'memberReputationMessages.mediumReputationTitle',
    defaultMessage: 'Medium Reputation'
  },
  LowMediumReputationNoKYCMessage: {
    id: 'memberReputationMessages.lowMediumReputationNoKYCMessage',
    defaultMessage: 'Boost your score by {certifying} your account.'
  },
  LowMediumReputationPendingKYCMessage: {
    id: 'memberReputationMessages.lowMediumReputationPendingKYCMessage',
    defaultMessage: 'Your {certification} is pending. If your {certification} is approved, your Reputation Score ' +
      'will increase by 30 points.'
  },
  LowMediumReputationKYCMessageForCurrentUser: {
    id: 'memberReputationMessages.lowMediumReputationKycMessageForCurrentUser',
    defaultMessage: 'Keep striving toward earning High Reputation! Any member who has a ' +
      ' Reputation Score of  ' + HIGH_REP_THRESHOLD +
      ' or more qualifies as High Reputation. High Rep members have additional' +
      ' privileges and qualify for Activity Reward bonuses.'
  },
  HighReputationTitle: {
    id: 'memberReputationMessages.highReputationTitle',
    defaultMessage: 'High Reputation'
  },
  HighReputationMessage: {
    id: 'memberReputationMessages.highReputationMessage',
    defaultMessage: 'Any member who has a Reputation Score of ' + HIGH_REP_THRESHOLD + ' or more qualifies as' +
      ' High Reputation. High Rep members have additional privileges and qualify for Activity Reward bonuses.'
  },
  ReputationBreakdownSectionTitle: {
    id: 'memberReputationMessages.reputationBreakdownSectionTitle',
    defaultMessage: 'Breakdown'
  },
  ReputationBreakdownSectionDescriptionForCurrentUser: {
    id: 'memberReputationMessages.reputationBreakdownSectionDescriptionForCurrentUser',
    defaultMessage: 'There are three components that determine your reputation score. The weighting is listed as' +
      ' a percentage next to each component below.'
  },
  ReputationBreakdownSectionDescription: {
    id: 'memberReputationMessages.reputationBreakdownSectionDescription',
    defaultMessage: 'There are three components that determine reputation score. The weighting is listed as' +
      ' a percentage next to each component below.'
  },
  ConductStatusTitle: {
    id: 'memberReputationMessages.conductStatusTitle',
    defaultMessage: 'Conduct Status (10%)'
  },
  ConductStatusDescription: {
    id: 'memberReputationMessages.conductStatusDescription',
    defaultMessage: 'If you are Conduct Negative, you\'ll earn 0 points. Otherwise, you\'ll earn 100.'
  },
  QualityAnalysisTitle: {
    id: 'memberReputationMessages.qualityAnalysisTitle',
    defaultMessage: 'Quality Analysis (60%)'
  },
  QualityAnalysisDescriptionForCurrentUser: {
    id: 'memberReputationMessages.qualityAnalysisDescriptionForCurrentUser',
    defaultMessage: 'A score that reflects the quality of your content, comments, followers, and more.'
  },
  QualityAnalysisDescription: {
    id: 'memberReputationMessages.qualityAnalysisDescription',
    defaultMessage: 'A score that reflects the quality of content, comments, followers, and more.'
  },
  CertifiedTitle: {
    id: 'memberReputationMessages.certifiedTitle',
    defaultMessage: 'Certified (30%)'
  },
  CertifiedDescriptionForCurrentUser: {
    id: 'memberReputationMessages.certifiedDescriptionForCurrentUser',
    defaultMessage: `If you become {certifiedLink}, you'll earn 100 points; otherwise 0.`
  },
  CertifiedDescription: {
    id: 'memberReputationMessages.certifiedDescription',
    defaultMessage: `{certifiedLink} members earn 100 points; otherwise 0.`
  },
  KycCertificationLink: {
    id: 'memberReputationMessages.kycCertificationLink',
    defaultMessage: 'Certification'
  },
  KycCertifiedLink: {
    id: 'memberReputationMessages.kycCertifiedLink',
    defaultMessage: 'Certified'
  },
  KycCertifyingLink: {
    id: 'memberReputationMessages.kycCertifyingLink',
    defaultMessage: 'Certifying'
  }
});
