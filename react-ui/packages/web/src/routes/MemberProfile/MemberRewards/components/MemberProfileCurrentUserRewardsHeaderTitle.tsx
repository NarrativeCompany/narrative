import * as React from 'react';
import { FormattedMessage } from 'react-intl';
import { compose, mapProps } from 'recompose';
import { withCurrentUserRewardsBalance, WithExtractedCurrentUserRewardsBalanceProps } from '@narrative/shared';
import { getRewardsHeaderTitle } from '../../../../shared/utils/rewardsUtils';
import { MemberRewardsMessages } from '../../../../shared/i18n/MemberRewardsMessages';
import { RewardsHeader, RewardsHeaderProps } from '../../../../shared/components/rewards/RewardsHeader';
import { RewardsMessages } from '../../../../shared/i18n/RewardsMessages';

const MemberProfileCurrentUserRewardsHeaderTitleComponent: React.SFC<RewardsHeaderProps> = (props) => {
  return (
    <RewardsHeader {...props}/>
  );
};

export const MemberProfileCurrentUserRewardsHeaderTitle = compose(
  withCurrentUserRewardsBalance,
  mapProps((props: WithExtractedCurrentUserRewardsBalanceProps): RewardsHeaderProps => {
    const { loading, rewardsBalance, ...ownProps } = props;
    return {
      title: getRewardsHeaderTitle(MemberRewardsMessages.NarrativePointBalance, loading, rewardsBalance, true),
      description: <FormattedMessage {...RewardsMessages.RevenuePayoutDescription}/>,
      ...ownProps
    } as RewardsHeaderProps;
  })
)(MemberProfileCurrentUserRewardsHeaderTitleComponent) as React.ComponentClass<RewardsHeaderProps>;
