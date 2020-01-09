import { defineMessages } from 'react-intl';

export const DeleteAccountMessages = defineMessages({
  PageTitle: {
    id: 'deleteAccountMessages.pageTitle',
    defaultMessage: 'Delete Account'
  },
  SummaryMessage: {
    id: 'deleteAccountMessages.summaryMessage',
    defaultMessage: 'Please confirm that you understand that account deletion is permanent.  '
  },
  ActionCannotBeUndone: {
    id: 'deleteAccountMessages.actionCannotBeUndone',
    defaultMessage: 'This action cannot be undone.'
  },
  IUnderstandLabel: {
    id: 'deleteAccountMessages.iUnderstandLabel',
    defaultMessage: 'I understand that...'
  },
  ProfileWillBeRemovedLabel: {
    id: 'deleteAccountMessages.profileWillBeRemovedLabel',
    defaultMessage: 'My profile and account information will be permanently removed.'
  },
  ContentWillBeRemovedLabel: {
    id: 'deleteAccountMessages.contentWillBeRemovedLabel',
    defaultMessage: 'My posts and comments will be permanently removed.'
  },
  NRVEBalanceLostLabel: {
    id: 'deleteAccountMessages.nrveBalanceLostLabel',
    defaultMessage: 'My {nrveLink} account balance will be irrevocably lost.'
  },
  NichesLostLabel: {
    id: 'deleteAccountMessages.nichesLostLabel',
    defaultMessage: 'I am the owner of {ownedNiches} {ownedNiches, plural, one {Niche} other {Niches}}. My ' +
      '{ownedNiches, plural, one {Niche} other {Niches}} will be placed for sale so someone else ' +
      'can take ownership in my place.'
  },
  PublicationsLostLabel: {
    id: 'deleteAccountMessages.publicationsLostLabel',
    defaultMessage: 'I am the owner of {ownedPublications} {ownedPublications, plural, one {Publication} other ' +
      '{Publications}}. The Publications I own will be permanently abandoned and expire at the end of my current ' +
      'paid-through period. Existing Admins, Editors, and Writers will continue to be able to use them until ' +
      'expiration, at which point the Publications will be permanently deleted.'
  },
  PublicationsLostDescriptionDeleteAccount: {
    id: 'deleteAccountMessages.publicationsLostDescriptionDeleteAccount',
    defaultMessage: 'If you\'d like someone else to take over ownership of a Publication, you should transfer ' +
      'ownership before deleting your account.'
  },
  PublicationsLostDescriptionRevokeAgreement: {
    id: 'deleteAccountMessages.publicationsLostDescriptionRevokeAgreement',
    defaultMessage: 'If you\'d like someone else to take over ownership of a Publication, you should transfer ' +
      'ownership before revoking agreement to the Terms of Service.'
  },
  Continue: {
    id: 'deleteAccountMessages.continue',
    defaultMessage: 'Continue'
  },
  TwoFactorSummaryMessageLinkText: {
    id: 'deleteAccountMessages.twoFactorSummaryMessageLinkText',
    defaultMessage: 'Google Authenticator app'
  },
  AvailableFor: {
    id: 'deleteAccountMessages.availableFor',
    defaultMessage: 'Available for {androidLink} and {iosLink}'
  },
  AcknowledgeTitle: {
    id: 'deleteAccountMessages.acknowledgeTitle',
    defaultMessage: 'Last Chance!'
  },
  AcknowledgeMessage: {
    id: 'deleteAccountMessages.acknowledgeMessage',
    defaultMessage: 'After this there is no turning back. Are you sure you want to delete your account permanently ' +
      'and lose your information and progress? Enter your password to delete your account.'
  },
  AcknowledgeMessageWithTwoFactorAuth: {
    id: 'deleteAccountMessages.acknowledgeMessageWithTwoFactorAuth',
    defaultMessage: 'After this there is no turning back. Are you sure you want to delete your account permanently' +
      ' and lose your information and progress? Enter your password and the 6-digit code from the {googleAuthApp} to' +
      ' delete your account.'
  },
  FinalAcknowledgeButtonLabel: {
    id: 'deleteAccountMessages.finalAcknowledgeTitle',
    defaultMessage: 'Delete My Account'
  },
  AccountDeleted: {
    id: 'deleteAccountMessages.accountDeleted',
    defaultMessage: 'Your account has been deleted'
  },
  Goodbye: {
    id: 'deleteAccountMessages.goodbye',
    defaultMessage: 'Goodbye!'
  },
});
