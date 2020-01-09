import * as React from 'react';
import { AboutSection } from '../components/AboutSection';
import { NicheExplainerMessages } from '../../../shared/i18n/NicheExplainerMessages';

export const BuySuggestedNicheSection: React.SFC<{}> = () => {
  return (
    <AboutSection
      title={NicheExplainerMessages.BuySuggestedNicheSectionTitle}
      titleType="niche"
      message={NicheExplainerMessages.BuySuggestedNicheParagraphOne}
    />
  );
};
