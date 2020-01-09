import * as React from 'react';
import { AboutSection } from '../components/AboutSection';
import { NicheExplainerMessages } from '../../../shared/i18n/NicheExplainerMessages';

export const NicheControllersSection: React.SFC<{}> = () => {
  return (
    <AboutSection
      title={NicheExplainerMessages.NicheControllersSectionTitle}
      titleType="niche"
      message={NicheExplainerMessages.NicheControllersParagraphOne}
    />
  );
};
