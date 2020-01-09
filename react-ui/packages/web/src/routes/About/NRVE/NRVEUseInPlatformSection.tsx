import * as React from 'react';
import { AboutSection } from '../components/AboutSection';
import { NRVEExplainerMessages } from '../../../shared/i18n/NRVEExplainerMessages';

export const NRVEUseInPlatformSection: React.SFC<{}> = () => {
  return (
    <AboutSection
      title={NRVEExplainerMessages.NRVEUseInPlatformSectionTitle}
      titleType="nrve"
      message={NRVEExplainerMessages.NRVEUseInPlatformParagraphOne}
    />
  );
};
