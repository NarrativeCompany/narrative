import gql from 'graphql-tag';
import { RewardValueFragment } from '../fragments/rewardValueFragment';

export const allTimePostRewardsQuery = gql`
  query AllTimePostRewardsQuery ($postOid: String!) {
    getAllTimePostRewards (postOid: $postOid)
    @rest(type: "RewardValue", path: "/posts/{args.postOid}/all-time-rewards") {
      ...RewardValue
    }
  }
  ${RewardValueFragment}
`;
