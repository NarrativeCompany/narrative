import { RewardsRow, RewardsWrapper } from '../../../../HQ/Reporting/Rewards/RewardsRow';
import { FormattedMessage } from 'react-intl';
import { RewardsMessages } from '../../../../../shared/i18n/RewardsMessages';
import { UsdAndNrveValue } from '../../../../../shared/components/rewards/UsdAndNrveValue';
import * as React from 'react';
import { compose } from 'recompose';
import { NicheRewardsMessages } from '../../../../../shared/i18n/NicheRewardsMessages';
import {
  NicheQueryParentProps,
  WithExtractedNicheRewardPeriodRewardsProps,
  withNicheRewardPeriodRewards
} from '@narrative/shared';
import { withLoadingPlaceholder } from '../../../../../shared/utils/withLoadingPlaceholder';
import { themeColors } from '../../../../../shared/styled/theme';

type ParentProps = NicheQueryParentProps & {
  month: string;
};

type Props = WithExtractedNicheRewardPeriodRewardsProps;

const NicheRewardsTableComponent: React.SFC<Props> = (props) => {
  const { rewardPeriodStats } = props;

  return (
    <RewardsWrapper style={{marginBottom: 30}}>
      <RewardsRow
        title={<FormattedMessage {...RewardsMessages.RewardPeriod}/>}
        value={rewardPeriodStats.rewardPeriodRange}
        style={{fontSize: 22}}
      />
      <RewardsRow
        title={<FormattedMessage {...NicheRewardsMessages.TotalOwnerRewards}/>}
        value={<UsdAndNrveValue nrveUsdValue={rewardPeriodStats.totalOwnerReward}/>}
      />
      <RewardsRow
        title={<FormattedMessage {...NicheRewardsMessages.TotalModeratorRewards}/>}
        value={<UsdAndNrveValue nrveUsdValue={rewardPeriodStats.totalModeratorReward}/>}
      />
      <RewardsRow
        title={<FormattedMessage {...NicheRewardsMessages.QualifyingPosts}/>}
        value={rewardPeriodStats.totalQualifyingPosts}
      />
      <RewardsRow
        title={<FormattedMessage {...NicheRewardsMessages.ExcludesLowQualityPosts}/>}
        value={''}
        style={{marginLeft: 25, color: themeColors.lightGray, fontSize: 14}}
      />
    </RewardsWrapper>
  );
};

export const NicheRewardsTable = compose(
  withNicheRewardPeriodRewards,
  withLoadingPlaceholder()
)(NicheRewardsTableComponent) as React.ComponentClass<ParentProps>;
