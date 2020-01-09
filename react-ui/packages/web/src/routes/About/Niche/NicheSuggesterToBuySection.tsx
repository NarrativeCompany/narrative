import * as React from 'react';
import { AboutSection } from '../components/AboutSection';
import { NicheExplainerMessages } from '../../../shared/i18n/NicheExplainerMessages';

export const NicheSuggesterToBuySection: React.SFC<{}> = () => {
  return (
    <AboutSection
      title={NicheExplainerMessages.NicheSuggesterToBuySectionTitle}
      titleType="niche"
      message={NicheExplainerMessages.NicheSuggesterToBuyParagraphOne}
    />
  );
};
