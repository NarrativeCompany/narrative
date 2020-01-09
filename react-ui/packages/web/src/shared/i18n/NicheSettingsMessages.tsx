import { defineMessages } from 'react-intl';

export const NicheSettingsMessages = defineMessages({
  Title: {
    id: 'nicheSettings.title',
    defaultMessage: 'Niche Settings'
  },
  SettingsSeoTitle: {
    id: 'nicheSettings.settingsSeoTitle',
    defaultMessage: 'Settings - {nicheName}'
  },
  SectionModeratorManagement: {
    id: 'nicheSettings.sectionModeratorSettings',
    defaultMessage: 'Moderator Management'
  },
  ModeratorCountLabel: {
    id: 'nicheSettings.moderatorCountLabel',
    defaultMessage: '# of Moderators'
  },
  ModeratorNominationsOpen: {
    id: 'nicheSettings.moderatorNominationsOpen',
    defaultMessage: 'Moderator Nominations Open'
  },
  CannotChangeDuringElectionMsg: {
    id: 'nicheSettings.cannotChangeDuringElectionMsg',
    defaultMessage: 'You cannot change this during a live election.'
  },
  ModeratorSlotsUpdateSuccessful: {
    id: 'nicheSettings.moderatorSlotsUpdateSuccessful',
    defaultMessage: 'Niche moderator slots successfully updated!'
  },
  NicheRenewalDate: {
    id: 'nicheSettings.nicheRenewalDate',
    defaultMessage: 'Niche Renewal Date'
  },
  NicheWillRenewAt: {
    id: 'nicheSettings.nicheWillRenewAt',
    defaultMessage: 'Your Niche will be up for renewal: '
  },
  NicheRenewalInfo: {
    id: 'nicheSettings.nicheRenewalInfo',
    defaultMessage: 'You will receive further details about the Niche renewal cost and process in advance of the ' +
      'renewal date.'
  },
  NicheProfileTitle: {
    id: 'nicheProfileSection.title',
    defaultMessage: 'Niche Profile'
  },
  NicheProfileDefinition: {
    id: 'nicheProfileSection.nicheProfileDefinition',
    defaultMessage: 'All Niches must be unique. However, a Niche may be a subset of an existing Niche, so long' +
      ' as there is a unique element to the Niche. For instance, "Lake Recreation" should be permitted, even' +
      ' if there is already a broader "Lakes" Niche. Do not include any profanity in your Niche name or' +
      ' definition and do not create a Niche that may violate the {termsOfService} or {acceptableUsePolicy}.'
  },
  NicheNameFieldLabel: {
    id: 'nicheProfileSection.nicheNameameFieldLabel',
    defaultMessage: 'Niche name'
  },
  NicheDescriptionFieldLabel: {
    id: 'nicheProfileSection.nicheDescriptionFieldLabel',
    defaultMessage: 'Niche definition'
  },
  FormHelpText: {
    id: 'nicheProfileSection.formHelpText',
    defaultMessage: 'Please note that approval of these changes will take a minimum of 48 hours. You will be' +
      ' unable to submit further changes until this request has been ruled on.'
  },
  OpenTribunalIssueMessage: {
    id: 'nicheProfileSection.outstandingTribunalIssueMessage',
    defaultMessage: 'Your Edit Request has been submitted.'
  },
  OpenTribunalIssueLink: {
    id: 'nicheProfileSection.outstandingTribunalIssueLink',
    defaultMessage: 'View Appeal'
  }
});
