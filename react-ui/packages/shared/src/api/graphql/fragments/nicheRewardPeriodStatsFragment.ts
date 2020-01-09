import gql from 'graphql-tag';
import { NrveUsdValueFragment } from './nrveUsdValueFragment';

export const NicheRewardPeriodStatsFragment = gql`
  fragment NicheRewardPeriodStats on NicheRewardPeriodStats {
    rewardPeriodRange

    totalOwnerReward @type(name: "NrveUsdValue") {
      ...NrveUsdValue
    }
    totalModeratorReward @type(name: "NrveUsdValue") {
      ...NrveUsdValue
    }
    totalQualifyingPosts
  }
  ${NrveUsdValueFragment}
`;
