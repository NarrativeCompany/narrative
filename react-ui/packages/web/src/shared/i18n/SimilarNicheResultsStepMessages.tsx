import { defineMessages } from 'react-intl';

export const SimilarNicheResultsStepMessages = defineMessages({
  PageHeaderTitle: {
    id: 'similarNicheReviewStep.pageHeaderTitle',
    defaultMessage: 'Review Potential Conflicts'
  },
  PageHeaderDescription: {
    id: 'similarNicheReviewStep.pageHeaderDescription',
    defaultMessage: 'Your Niche must be unique or a subset of another Niche. If not, your suggested Niche could be ' +
      'rejected by the community during the approval process. Please verify your suggestion does not conflict' +
      ' with any Niches below:'
  },
  BackBtnText: {
    id: 'similarNicheReviewStep.backBtnText',
    defaultMessage: 'Suggest Niche'
  },
  NoSimilarNichesText: {
    id: 'similarNicheReviewStep.noSimilarNichesText',
    defaultMessage: 'We didn’t find any conflicts!'
  }
});
