import gql from 'graphql-tag';
import { NrveUsdValueFragment } from './nrveUsdValueFragment';
import { NicheOwnershipRewardFragment } from './nicheOwnershipRewardFragment';

export const UserRewardPeriodStatsFragment = gql`
  fragment UserRewardPeriodStats on UserRewardPeriodStats {
    rewardPeriodRange

    totalContentCreationReward @type(name: "NrveUsdValue") {
      ...NrveUsdValue
    }
    totalNicheOwnershipReward @type(name: "NrveUsdValue") {
      ...NrveUsdValue
    }
    totalNicheModerationReward @type(name: "NrveUsdValue") {
      ...NrveUsdValue
    }
    totalActivityRewards @type(name: "NrveUsdValue") {
      ...NrveUsdValue
    }
    activityBonusPercentage
    totalElectorateReward @type(name: "NrveUsdValue") {
      ...NrveUsdValue
    }
    totalTribunalReward @type(name: "NrveUsdValue") {
      ...NrveUsdValue
    }
    totalReward @type(name: "NrveUsdValue") {
      ...NrveUsdValue
    }
  
    nicheOwnershipRewards @type(name: "NicheOwnershipReward") {
      ...NicheOwnershipReward
    }
  
    percentageOfTotalPayout
  }
  ${NrveUsdValueFragment}
  ${NicheOwnershipRewardFragment}
`;
