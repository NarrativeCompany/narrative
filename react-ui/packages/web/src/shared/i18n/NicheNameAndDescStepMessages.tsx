import { defineMessages } from 'react-intl';

export const NicheNameAndDescStepMessages = defineMessages({
  PageHeaderTitle: {
    id: 'nicheNameAndDescStep.pageHeaderTitle',
    defaultMessage: 'Suggest a Niche'
  },
  PageHeaderDescription: {
    id: 'nicheNameAndDescStep.pageHeaderDescription',
    defaultMessage: 'Be sure your Niche is unique or can be a subset of another Niche. Niche names and' +
      ' definitions must be clearly defined in English, must be spelled properly, cannot include profanity, and must ' +
      'not violate the {termsOfService} or {acceptableUsePolicy}.'
  },
  PageHeaderDescriptionYouCanSuggestOneNichePerDay: {
    id: 'nicheNameAndDescStep.pageHeaderDescriptionYouCanSuggestOneNichePerDay',
    defaultMessage: 'You can only suggest {boldText}.'
  },
  PageHeaderDescriptionBold: {
    id: 'nicheNameAndDescStep.pageHeaderDescriptionBold',
    defaultMessage: 'one Niche per day'
  },
  NameFieldPlaceholder: {
    id: 'nicheNameAndDescStep.nameFieldPlaceholder',
    defaultMessage: 'Example: “Cars,” “Surfing,” “Photography,”  etc'
  },
  DefinitionFieldPlaceholder: {
    id: 'nicheNameAndDescStep.definitionFieldPlaceholder',
    defaultMessage: 'Define the Niche as clearly and succinctly as possible.'
  },
});
