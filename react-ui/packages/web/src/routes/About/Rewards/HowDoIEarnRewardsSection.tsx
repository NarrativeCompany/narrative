import * as React from 'react';
import { AboutSection } from '../components/AboutSection';
import { AboutSectionParagraph } from '../components/AboutSectionParagraph';
import { RewardsExplainerMessages } from '../../../shared/i18n/RewardsExplainerMessages';
import pieChart from '../../../assets/pie-chart.png';
import styled from '../../../shared/styled';
import { FlexContainer, FlexContainerProps } from '../../../shared/styled/shared/containers';

const ImgWrapper = styled<FlexContainerProps>(FlexContainer)`
  img {
    max-width: 550px;
    width: 100%;
  }
`;

export const HowDoIEarnRewardsSection: React.SFC<{}> = () => {
  return (
    <AboutSection title={RewardsExplainerMessages.HowDoIEarnRewardsSectionTitle}>
      <AboutSectionParagraph message={RewardsExplainerMessages.HowDoIEarnRewardsParagraphOne} />
      <AboutSectionParagraph message={RewardsExplainerMessages.HowDoIEarnRewardsParagraphTwo} />

      <ImgWrapper centerAll={true}>
        <img src={pieChart} alt=""/>
      </ImgWrapper>
    </AboutSection>
  );
};
