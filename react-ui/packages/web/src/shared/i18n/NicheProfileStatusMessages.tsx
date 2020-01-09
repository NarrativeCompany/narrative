import { defineMessages } from 'react-intl';

export const NicheProfileStatusMessages = defineMessages({
  NicheStatusSectionTitle: {
    id: 'nicheProfileMessages.nicheStatusSectionTitle',
    defaultMessage: 'Status'
  },
  AppealRejectedNicheQuestion: {
    id: 'nicheProfileMessages.appealRejectedNicheQuestion',
    defaultMessage: 'Do you believe that this Niche is valid and should be approved? If so, you may appeal to the ' +
      'Tribunal to have this Niche approved.'
  },
  AppealActiveNicheQuestion: {
    id: 'nicheProfileMessages.appealActiveNicheQuestion',
    defaultMessage: 'Do you believe that this Niche violates the {termsOfServiceLink} or {acceptableUsePolicyLink}? ' +
      'If so, you may appeal to the Tribunal to have this Niche officially rejected.'
  },
  AppealRejectedNicheNote: {
    id: 'nicheProfileMessages.appealRejectedNicheNote',
    defaultMessage: 'Please note that if you appeal a Niche and your appeal is not upheld by the Tribunal, your own ' +
      'reputation will be negatively impacted. Thus, you should only file an appeal if you feel strongly that the ' +
      'Niche should be approved.'
  },
  AppealActiveNicheNote: {
    id: 'nicheProfileMessages.appealActiveNicheNote',
    defaultMessage: 'Please note that if you appeal a Niche and your appeal is not upheld by the Tribunal, your own ' +
      'reputation will be negatively impacted. Thus, you should only file an appeal if you feel strongly that the ' +
      'Niche is in violation of our policies.'
  },
  AppealToTribunalToApprove: {
    id: 'nicheProfileStatusMessages.appealToTribunalToApprove',
    defaultMessage: 'Appeal to Tribunal To Approve This Niche'
  },
  AppealToTribunalToReject: {
    id: 'nicheProfileStatusMessages.appealToTribunalToReject',
    defaultMessage: 'Appeal to Tribunal To Reject This Niche'
  },
});
