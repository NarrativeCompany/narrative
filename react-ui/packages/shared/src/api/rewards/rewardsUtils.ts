import { RewardPeriod, NrveUsdValue } from '../../types';
import { LoadingProps } from '../../utils';

export interface WithExtractedAllTimeRewardsProps extends LoadingProps {
  allTimeRewards: NrveUsdValue;
}

export interface WithExtractedRewardPeriodsProps extends LoadingProps {
  rewardPeriods: RewardPeriod[];
}
