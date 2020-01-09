import * as React from 'react';
import { AboutSection } from '../components/AboutSection';
import { NRVEExplainerMessages } from '../../../shared/i18n/NRVEExplainerMessages';
import { geeksOnly } from './NRVEExplainer';
import { AboutSectionParagraph } from '../components/AboutSectionParagraph';

export const StoreNRVESection: React.SFC<{}> = () => {
  return (
    <AboutSection title={NRVEExplainerMessages.StoreNRVESectionTitle} titleType="nrve">
      <AboutSectionParagraph message={NRVEExplainerMessages.StoreNRVEParagraphOne}/>
      <AboutSectionParagraph message={NRVEExplainerMessages.StoreNRVEParagraphTwo}/>
      <AboutSectionParagraph message={NRVEExplainerMessages.StoreNRVEParagraphThree} values={{ geeksOnly }}/>
    </AboutSection>
  );
};
