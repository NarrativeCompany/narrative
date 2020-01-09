import * as React from 'react';
import { AboutSection } from '../components/AboutSection';
import { NicheExplainerMessages } from '../../../shared/i18n/NicheExplainerMessages';

export const NicheContentSubmissionSection: React.SFC<{}> = () => {
  return (
    <AboutSection
      title={NicheExplainerMessages.NicheContentSubmissionSectionTitle}
      titleType="niche"
      message={NicheExplainerMessages.NicheContentSubmissionParagraphOne}
    />
  );
};
