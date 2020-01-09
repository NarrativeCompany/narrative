import gql from 'graphql-tag';
import { RewardValueFragment } from '../fragments/rewardValueFragment';

export const currentUserRewardsBalanceQuery = gql`
  query CurrentUserRewardsBalanceQuery {
    getCurrentUserRewardsBalance @rest(type: "RewardValue", path: "/users/current/rewards-balance") {
      ...RewardValue
    }
  }
  ${RewardValueFragment}
`;
