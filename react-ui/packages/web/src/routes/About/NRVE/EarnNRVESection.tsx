import * as React from 'react';
import { AboutSection } from '../components/AboutSection';
import { NRVEExplainerMessages } from '../../../shared/i18n/NRVEExplainerMessages';
import pieChart from '../../../assets/pie-chart.png';
import styled from '../../../shared/styled';
import { FlexContainer, FlexContainerProps } from '../../../shared/styled/shared/containers';
import { AboutSectionParagraph } from '../components/AboutSectionParagraph';

const ImgWrapper = styled<FlexContainerProps>(FlexContainer)`
  img {
    max-width: 550px;
    width: 100%;
  }
`;

export const EarnNRVESection: React.SFC<{}> = () => {
  return (
    <AboutSection title={NRVEExplainerMessages.EarnNRVESectionTitle} titleType="nrve">
      <AboutSectionParagraph message={NRVEExplainerMessages.EarnNRVEParagraphOne}/>
      <AboutSectionParagraph message={NRVEExplainerMessages.EarnNRVEParagraphTwo} style={{ fontWeight: 'bold' }}/>

      <ImgWrapper centerAll={true}>
        <img src={pieChart} alt=""/>
      </ImgWrapper>
    </AboutSection>
  );
};
