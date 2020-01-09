import * as React from 'react';
import { AboutSection } from '../components/AboutSection';
import { NicheExplainerMessages } from '../../../shared/i18n/NicheExplainerMessages';
import { AboutSectionParagraph } from '../components/AboutSectionParagraph';

export const NicheOwnershipSection: React.SFC<{}> = () => {
  return (
    <AboutSection title={NicheExplainerMessages.NicheOwnershipSectionTitle} titleType="niche">
      <AboutSectionParagraph message={NicheExplainerMessages.NicheOwnershipParagraphOne}/>
      <AboutSectionParagraph message={NicheExplainerMessages.NicheOwnershipParagraphTwo}/>
    </AboutSection>
  );
};
