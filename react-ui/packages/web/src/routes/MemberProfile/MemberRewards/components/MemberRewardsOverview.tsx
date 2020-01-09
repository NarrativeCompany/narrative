import * as React from 'react';
import { compose, withHandlers, withProps } from 'recompose';
import { MemberProfileConnect, WithMemberProfileProps } from '../../../../shared/context/MemberProfileContext';
import { getQueryArg } from '@narrative/shared';
import { injectIntl } from 'react-intl';
import { RewardPeriodsConnect, WithRewardPeriodsProps } from '../../../../shared/context/RewardPeriodsContext';
import { generatePath, RouteComponentProps, withRouter } from 'react-router';
import { SelectValue } from 'antd/lib/select';
import { createUrl } from '../../../../shared/utils/routeUtils';
import { WebRoute } from '../../../../shared/constants/routes';
import { SEO } from '../../../../shared/components/SEO';
import { MemberRewardsMessages } from '../../../../shared/i18n/MemberRewardsMessages';
import InjectedIntlProps = ReactIntl.InjectedIntlProps;
import { RewardPeriodSelector } from '../../../../shared/components/rewards/RewardPeriodSelector';
import { MemberRewardsTable } from './MemberRewardsTable';

const monthParam = 'month';

type Props =
  InjectedIntlProps &
  WithMemberProfileProps &
  WithRewardPeriodsProps & {
  month: string;
  onMonthChange: (value: SelectValue) => void;
};

const MemberRewardsOverviewComponent: React.SFC<Props> = (props) => {
  const { detailsForProfile: { user }, onMonthChange, rewardPeriods, intl: { formatMessage } } = props;
  const { displayName } = user;

  const month = props.month && rewardPeriods.find(rp => rp.yearMonth === props.month) ?
    props.month :
    rewardPeriods[0].yearMonth;

  return (
    <React.Fragment>
      <SEO title={formatMessage(MemberRewardsMessages.OverviewSeoTitle, {displayName})} />
      <RewardPeriodSelector
        loading={false}
        rewardPeriods={rewardPeriods}
        month={month}
        onChange={onMonthChange}
        includeExcludedMonthsNote={true}
      />
      <MemberRewardsTable user={user} month={month}/>
    </React.Fragment>
  );
};

export default compose(
  MemberProfileConnect,
  RewardPeriodsConnect,
  injectIntl,
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
      const { username } = props.detailsForProfile.user;

      const newURL = createUrl(
          generatePath(WebRoute.UserProfileRewards, {username}),
          { [monthParam]: value }
        );

      props.history.push(newURL);
    }
  }),
)(MemberRewardsOverviewComponent) as React.ComponentClass<{}>;
