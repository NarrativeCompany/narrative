import gql from 'graphql-tag';
import { RewardPeriodStatsFragment } from '../fragments/rewardPeriodStatsFragment';

export const rewardPeriodStatsQuery = gql`
  query RewardPeriodStatsQuery($input: RewardPeriodInput) {
    getRewardPeriodStats (input: $input)
    @rest(type: "RewardPeriodStats", path: "/rewards/period-stats?{args.input}") {
      ...RewardPeriodStats
    }
  }
  ${RewardPeriodStatsFragment}
`;
