import * as React from 'react';
import { AboutSection } from '../components/AboutSection';
import { AboutSectionParagraph } from '../components/AboutSectionParagraph';
import { RewardsExplainerMessages } from '../../../shared/i18n/RewardsExplainerMessages';
import { Link } from '../../../shared/components/Link';
import { FormattedMessage } from 'react-intl';

export const HowMuchAreRewardsWorthSection: React.SFC<{}> = () => {
  const nrveLink = <Link.About type="nrve" size="inherit"/>;
  const readAboutNrveHereLink = (
    <Link.About type="nrve" size="inherit">
      <FormattedMessage {...RewardsExplainerMessages.ReadAboutNrveHere} />
    </Link.About>
  );

  return (
    <AboutSection
      title={RewardsExplainerMessages.HowMuchAreRewardsWorthSectionTitle}
      message={RewardsExplainerMessages.HowMuchAreRewardsWorthParagraphOne}
      messageValues={{ nrveLink, readAboutNrveHereLink }}
    >
      <AboutSectionParagraph message={RewardsExplainerMessages.HowMuchAreRewardsWorthParagraphTwo} />
    </AboutSection>
  );
};
