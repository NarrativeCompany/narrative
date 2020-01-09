import gql from 'graphql-tag';
import { UserRewardPeriodStatsFragment } from '../fragments/userRewardPeriodStatsFragment';

export const userRewardPeriodRewardsQuery = gql`
  query UserRewardPeriodRewardsQuery($userOid: String!, $input: RewardPeriodInput) {
    getUserRewardPeriodRewards (userOid: $userOid, input: $input)
    @rest(type: "UserRewardPeriodStats", path: "/users/{args.userOid}/period-rewards?{args.input}") {
      ...UserRewardPeriodStats
    }
  }
  ${UserRewardPeriodStatsFragment}
`;
