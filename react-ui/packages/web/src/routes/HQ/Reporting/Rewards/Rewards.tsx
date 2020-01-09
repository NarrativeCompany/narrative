import * as React from 'react';
import { compose, withHandlers, withProps } from 'recompose';
import { FormattedMessage } from 'react-intl';
import { SEO } from '../../../../shared/components/SEO';
import { PageHeader } from '../../../../shared/components/PageHeader';
import { SEOMessages } from '../../../../shared/i18n/SEOMessages';
import { RewardsMessages } from '../../../../shared/i18n/RewardsMessages';
import {
  UnderstandingNarrativePointsSection
} from '../../../../shared/components/rewards/UnderstandingNarrativePointsSection';
import { RewardsBody } from './RewardsBody';
import { getQueryArg } from '@narrative/shared';
import { RouteComponentProps, withRouter } from 'react-router';
import { SelectValue } from 'antd/lib/select';
import { createUrl } from '../../../../shared/utils/routeUtils';
import { WebRoute } from '../../../../shared/constants/routes';
import { AllRewardPeriodsSelector } from '../../../../shared/components/rewards/AllRewardPeriodsSelector';
import { HQRewardsHeader } from './HQRewardsHeader';

export const monthParam = 'month';

interface Props {
  month: string;
  onMonthChange: (value: SelectValue) => void;
}

const RewardsComponent: React.SFC<Props> = (props) => {
  const { month, onMonthChange } = props;

  return (
    <React.Fragment>
      <SEO
        title={SEOMessages.RewardsTitle}
        description={SEOMessages.RewardsDescription}
      />

      <PageHeader
        iconType="network-stats"
        preTitle={<FormattedMessage {...RewardsMessages.PageHeaderPreTitle}/>}
        title={<FormattedMessage {...RewardsMessages.PageHeaderTitle}/>}
      />

      <HQRewardsHeader/>
      <AllRewardPeriodsSelector month={month} onChange={onMonthChange}/>
      <RewardsBody month={month}/>
      <UnderstandingNarrativePointsSection/>

    </React.Fragment>
  );
};

export default compose(
  withRouter,
  withProps((props: RouteComponentProps<{}>) => {
    const { location: { search } } = props;

    const month = getQueryArg(search, monthParam);

    return { month };
  }),
  withHandlers({
    onMonthChange: (
      props: Props & RouteComponentProps<{}>
    ) => (value: SelectValue) => {
      const newURL = createUrl(
          WebRoute.NetworkStatsRewards,
          { [monthParam]: value }
        );

      props.history.push(newURL);
    }
  }),
)(RewardsComponent) as React.ComponentClass<{}>;
