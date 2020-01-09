import * as React from 'react';
import { AboutSection } from '../components/AboutSection';
import { RewardsExplainerMessages } from '../../../shared/i18n/RewardsExplainerMessages';
import { Link } from '../../../shared/components/Link';

export const WhatAreNarrativesWalletsSection: React.SFC<{}> = () => {
  const nrveWalletsLink = <Link.About type="nrveWallets"/>;
  return (
    <AboutSection
      title={RewardsExplainerMessages.WhatAreNarrativesWalletsSectionTitle}
      message={RewardsExplainerMessages.WhatAreNarrativesWalletsParagraphOne}
      messageValues={{nrveWalletsLink}}
    />
  );
};
