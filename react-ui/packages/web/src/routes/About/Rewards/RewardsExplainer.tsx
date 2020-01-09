import * as React from 'react';
import { FormattedMessage, injectIntl } from 'react-intl';
import { ViewWrapper } from '../../../shared/components/ViewWrapper';
import { DetailsGradientBox } from '../../../shared/components/DetailsGradientBox';
import { AboutPageHeader } from '../components/AboutPageHeader';
import { RewardsExplainerMessages } from '../../../shared/i18n/RewardsExplainerMessages';
import { HowDoIEarnRewardsSection } from './HowDoIEarnRewardsSection';
import { RewardsBalanceSection } from './RewardsBalanceSection';
import { HowMuchAreRewardsWorthSection } from './HowMuchAreRewardsWorthSection';
import { CanILoseRewardsSection } from './CanILoseRewardsSection';
import { CanIShareRewardsSection } from './CanIShareRewardsSection';
import { RewardsGeeksOnlySection } from './RewardsGeeksOnlySection';
import { compose } from 'recompose';
import { InjectedIntlProps } from 'react-intl';
import { HowOftenDoIGetRewards } from './HowOftenDoIGetRewardsSection';
import { WhatAreNarrativesWalletsSection } from './WhatAreNarrativesWalletsSection';

export const geeksOnly = (
  <strong>
    <FormattedMessage {...RewardsExplainerMessages.GeeksOnly}/>
  </strong>
);

const RewardsExplainerComponent: React.SFC<InjectedIntlProps> = (props) => {
  const { intl: { formatMessage }} = props;

  return (
    <ViewWrapper
      maxWidth={890}
      style={{ paddingTop: 75 }}
      gradientBox={<DetailsGradientBox color="darkBlue" size="large"/>}
    >
      <AboutPageHeader
        seoTitle={formatMessage(RewardsExplainerMessages.SEOTitle)}
        title={<FormattedMessage {...RewardsExplainerMessages.PageHeaderTitle}/>}
        description={<FormattedMessage {...RewardsExplainerMessages.PageHeaderDescription}/>}
      />

      <HowDoIEarnRewardsSection/>
      <HowOftenDoIGetRewards/>
      <RewardsBalanceSection/>
      <HowMuchAreRewardsWorthSection/>
      <CanILoseRewardsSection/>
      <CanIShareRewardsSection/>
      <WhatAreNarrativesWalletsSection/>
      <RewardsGeeksOnlySection/>
    </ViewWrapper>
  );
};

const RewardsExplainer = compose(
  injectIntl
)(RewardsExplainerComponent) as React.ComponentClass<{}>;

export default RewardsExplainer;
