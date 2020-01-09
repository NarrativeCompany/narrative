import gql from 'graphql-tag';

import { NrveUsdValueFragment } from './nrveUsdValueFragment';
import { PostFragment } from './postFragment';

export const RewardLeaderboardPostFragment = gql`
  fragment RewardLeaderboardPost on RewardLeaderboardPost {
    postOid
    post @type(name: "Post") {
      ...Post
    }
    reward @type(name: "NrveUsdValue") {
      ...NrveUsdValue
    }
  }
  ${NrveUsdValueFragment}
  ${PostFragment}
`;
