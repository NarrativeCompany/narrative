import gql from 'graphql-tag';
import { NicheRewardPeriodStatsFragment } from '../fragments/nicheRewardPeriodStatsFragment';

export const nicheRewardPeriodRewardsQuery = gql`
  query NicheRewardPeriodRewardsQuery($nicheOid: String!, $input: RewardPeriodInput) {
    getNicheRewardPeriodRewards (nicheOid: $nicheOid, input: $input)
    @rest(type: "NicheRewardPeriodStats", path: "/niches/{args.nicheOid}/period-rewards?{args.input}") {
      ...NicheRewardPeriodStats
    }
  }
  ${NicheRewardPeriodStatsFragment}
`;
