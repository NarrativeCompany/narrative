import { defineMessages } from 'react-intl';

export const GuidelinesModalMessages = defineMessages({
  Title: {
    id: 'guidelinesModal.modalTitle',
    defaultMessage: 'Guidelines'
  },
  ParagraphOne: {
    id: 'guidelinesModal.paragraphOne',
    defaultMessage: 'Before being put up for auction, each Niche must be approved by the community.'
  },
  SectionOneHeader: {
    id: 'guidelinesModal.sectionOneHeader',
    defaultMessage: 'Vote Up {arrowIcon} If:'
  },
  SectionOneParagraphOne: {
    id: 'guidelinesModal.sectionOneParagraphOne',
    defaultMessage: '- The Niche is unique in some way. A Niche can be a subset of another Niche, however. For' +
      ' example, "Wireless Routers" is perfectly valid, even if "Routers" already exists.'
  },
  SectionOneParagraphTwo: {
    id: 'guidelinesModal.sectionOneParagraphTwo',
    defaultMessage: '- You think some people will find the Niche interesting, even if you personally have no ' +
      'interest in it.'
  },
  SectionTwoHeader: {
    id: 'guidelinesModal.sectionTwoHeader',
    defaultMessage: 'Vote Down {arrowIcon} If:'
  },
  SectionTwoParagraphOne: {
    id: 'guidelinesModal.sectionTwoParagraphOne',
    defaultMessage: '- The Niche is not unique. For example, "NY Giants Football Team" is not unique if "New York' +
      ' Giants Football Team" already exists.'
  },
  SectionTwoParagraphTwo: {
    id: 'guidelinesModal.sectionTwoParagraphTwo',
    defaultMessage: '- You think the subject is morally objectionable or violates our Terms of Service. Political' +
      ' or religious differences should be given wide latitude.'
  },
  SectionTwoParagraphThree: {
    id: 'guidelinesModal.sectionTwoParagraphThree',
    defaultMessage: '- For example, if you are "Pro-Choice," you should not reject a "Pro-Life" Niche. Political' +
      ' and religious differences should not be grounds for rejection.'
  }
});
