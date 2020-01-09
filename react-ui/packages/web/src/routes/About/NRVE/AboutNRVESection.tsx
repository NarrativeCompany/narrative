import * as React from 'react';
import { AboutSection } from '../components/AboutSection';
import { NRVEExplainerMessages } from '../../../shared/i18n/NRVEExplainerMessages';
import { geeksOnly } from './NRVEExplainer';
import { AboutSectionParagraph } from '../components/AboutSectionParagraph';

export const AboutNRVESection: React.SFC<{}> = () => {
  return (
    <AboutSection title={NRVEExplainerMessages.AboutNRVESectionTitle} titleType="nrve">
      <AboutSectionParagraph message={NRVEExplainerMessages.AboutNRVEParagraphOne} />
      <AboutSectionParagraph message={NRVEExplainerMessages.AboutNRVEParagraphTwo} />
      <AboutSectionParagraph message={NRVEExplainerMessages.AboutNRVEParagraphThree} values={{ geeksOnly }} />
    </AboutSection>
  );
};
