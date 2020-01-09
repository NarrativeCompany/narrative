import * as React from 'react';
import { WithRewardPeriodStatsProps } from './RewardsBody';
import { RewardsMessages } from '../../../../shared/i18n/RewardsMessages';
import { FormattedMessage } from 'react-intl';
import { UsdAndNrveValue } from '../../../../shared/components/rewards/UsdAndNrveValue';
import { RewardsRow, RewardsWrapper } from './RewardsRow';

export const RewardsCreditedTable: React.SFC<WithRewardPeriodStatsProps> = (props) => {
  const { rewardPeriodStats } = props;

  return (
    <RewardsWrapper style={{marginBottom: 30}}>
      <RewardsRow
        title={<FormattedMessage {...RewardsMessages.RewardPeriod}/>}
        value={rewardPeriodStats.rewardPeriodRange}
        style={{fontSize: 22}}
      />
      <RewardsRow
        title={<FormattedMessage {...RewardsMessages.ContentCreation}/>}
        value={<UsdAndNrveValue nrveUsdValue={rewardPeriodStats.contentCreatorReward}/>}
        percentage={60}
      />
      <RewardsRow
        title={<FormattedMessage {...RewardsMessages.NarrativeCompany}/>}
        value={<UsdAndNrveValue nrveUsdValue={rewardPeriodStats.narrativeCompanyReward}/>}
        percentage={15}
      />
      <RewardsRow
        title={<FormattedMessage {...RewardsMessages.NicheOwnership}/>}
        value={<UsdAndNrveValue nrveUsdValue={rewardPeriodStats.nicheOwnershipReward}/>}
        percentage={10}
      />
      <RewardsRow
        title={<FormattedMessage {...RewardsMessages.ActivityRewards}/>}
        value={<UsdAndNrveValue nrveUsdValue={rewardPeriodStats.activityRewards}/>}
        percentage={9}
      />
      <RewardsRow
        title={<FormattedMessage {...RewardsMessages.NicheModeration}/>}
        value={<UsdAndNrveValue nrveUsdValue={rewardPeriodStats.nicheModerationReward}/>}
        percentage={6}
      />
      <RewardsRow
        title={<FormattedMessage {...RewardsMessages.Tribunal}/>}
        value={<UsdAndNrveValue nrveUsdValue={rewardPeriodStats.tribunalReward}/>}
        percentage={0}
      />
      <RewardsRow
        title={<FormattedMessage {...RewardsMessages.TotalPointsCredited}/>}
        value={<UsdAndNrveValue nrveUsdValue={rewardPeriodStats.totalRewards}/>}
        style={{fontSize: 22, fontWeight: 600}}
      />
    </RewardsWrapper>
  );
};
