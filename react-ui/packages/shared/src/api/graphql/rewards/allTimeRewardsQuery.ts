import gql from 'graphql-tag';
import { RewardValueFragment } from '../fragments/rewardValueFragment';

export const allTimeRewardsQuery = gql`
  query AllTimeRewardsQuery {
    getAllTimeRewards @rest(type: "RewardValue", path: "/rewards/all-time-rewards") {
      ...RewardValue
    }
  }
  ${RewardValueFragment}
`;
