import * as React from 'react';
import { AboutSection } from '../components/AboutSection';
import { AboutSectionParagraph } from '../components/AboutSectionParagraph';
import { RewardsExplainerMessages } from '../../../shared/i18n/RewardsExplainerMessages';

export const CanIShareRewardsSection: React.SFC<{}> = () => {
  return (
    <AboutSection title={RewardsExplainerMessages.CanIShareRewardsSectionTitle}>
      <AboutSectionParagraph message={RewardsExplainerMessages.CanIShareRewardsParagraphOne} />
    </AboutSection>
  );
};
