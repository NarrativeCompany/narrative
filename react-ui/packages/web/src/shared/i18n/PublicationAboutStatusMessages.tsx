import { defineMessages } from 'react-intl';

export const PublicationAboutStatusMessages = defineMessages({
  StatusSectionTitle: {
    id: 'publicationAboutStatusMessages.statusSectionTitle',
    defaultMessage: 'Status'
  },
  StatusIntro: {
    id: 'publicationAboutStatusMessages.statusIntro',
    defaultMessage: 'This Publication was created on {creationDatetime} and currently has ' +
      '{followerCount} {followerCount, plural, one {follower} other {followers}}.'
  },
  AppealPublicationNote: {
    id: 'publicationAboutStatusMessages.appealPublicationNote',
    defaultMessage: 'If you feel that this Publication violates the {termsOfServiceLink} or ' +
      '{acceptableUsePolicyLink}, you may appeal to the Tribunal to have it removed from the network. Please note ' +
      'that, if you file an appeal and it is not upheld by the Tribunal, your own reputation will be negatively ' +
      'impacted. Thus, you should only file an appeal if you feel strongly that the Publication is in violation ' +
      'of our policies.'
  },
  AppealToTribunalToReject: {
    id: 'publicationAboutStatusMessages.appealToTribunalToReject',
    defaultMessage: 'Appeal to Tribunal To Reject This Publication'
  },
});
