import gql from 'graphql-tag';
import { RewardPeriodFragment } from '../fragments/rewardPeriodFragment';

export const userRewardPeriodsQuery = gql`
  query UserRewardPeriodsQuery ($input: UserOidInput!) {
    getUserRewardPeriods (input: $input)
    @rest(
      type: "RewardPeriod",
      path: "/users/{args.input.userOid}/reward-periods"
    ) 
    {
      ...RewardPeriod
    }
  }
  ${RewardPeriodFragment}
`;
