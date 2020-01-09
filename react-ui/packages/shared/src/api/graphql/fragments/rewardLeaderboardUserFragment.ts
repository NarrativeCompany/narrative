import gql from 'graphql-tag';

import { NrveUsdValueFragment } from './nrveUsdValueFragment';
import { UserFragment } from './userFragment';

export const RewardLeaderboardUserFragment = gql`
  fragment RewardLeaderboardUser on RewardLeaderboardUser {
    user @type(name: "User") {
      ...User
    }
    reward @type(name: "NrveUsdValue") {
      ...NrveUsdValue
    }
  }
  ${NrveUsdValueFragment}
  ${UserFragment}
`;
