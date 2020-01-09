import gql from 'graphql-tag';
import { NrveUsdValueFragment } from './nrveUsdValueFragment';

export const RewardPeriodStatsFragment = gql`
  fragment RewardPeriodStats on RewardPeriodStats {
    rewardPeriodRange
    contentCreatorReward @type(name: "NrveUsdValue") {
      ...NrveUsdValue
    }
    narrativeCompanyReward @type(name: "NrveUsdValue") {
      ...NrveUsdValue
    }
    nicheOwnershipReward @type(name: "NrveUsdValue") {
      ...NrveUsdValue
    }
    activityRewards @type(name: "NrveUsdValue") {
      ...NrveUsdValue
    }
    nicheModerationReward @type(name: "NrveUsdValue") {
      ...NrveUsdValue
    }
    electorateReward @type(name: "NrveUsdValue") {
      ...NrveUsdValue
    }
    tribunalReward @type(name: "NrveUsdValue") {
      ...NrveUsdValue
    }
    totalRewards @type(name: "NrveUsdValue") {
      ...NrveUsdValue
    }

    nicheOwnershipFeeRevenue @type(name: "NrveUsdValue") {
      ...NrveUsdValue
    }
    publicationOwnershipFeeRevenue @type(name: "NrveUsdValue") {
      ...NrveUsdValue
    }
    tokenMintRevenue @type(name: "NrveUsdValue") {
      ...NrveUsdValue
    }
    advertisingRevenue @type(name: "NrveUsdValue") {
      ...NrveUsdValue
    }
    miscellaneousRevenue @type(name: "NrveUsdValue") {
      ...NrveUsdValue
    }
    carryoverRevenue @type(name: "NrveUsdValue") {
      ...NrveUsdValue
    }
    totalRevenue @type(name: "NrveUsdValue") {
      ...NrveUsdValue
    }
  }
  ${NrveUsdValueFragment}
`;
