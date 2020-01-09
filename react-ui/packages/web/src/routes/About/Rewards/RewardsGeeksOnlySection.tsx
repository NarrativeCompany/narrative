import * as React from 'react';
import { AboutSectionParagraph } from '../components/AboutSectionParagraph';
import { RewardsExplainerMessages } from '../../../shared/i18n/RewardsExplainerMessages';
import { geeksOnly } from './RewardsExplainer';
import { AboutSection } from '../components/AboutSection';

export const RewardsGeeksOnlySection: React.SFC<{}> = () => {
  return (
    <AboutSection>
      <AboutSectionParagraph message={RewardsExplainerMessages.GeeksOnlyNotCryptoWallet} values={{geeksOnly}} />
    </AboutSection>
  );
};
