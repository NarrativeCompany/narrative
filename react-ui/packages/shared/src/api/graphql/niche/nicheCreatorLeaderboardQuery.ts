import gql from 'graphql-tag';
import { RewardLeaderboardUserFragment } from '../fragments/rewardLeaderboardUserFragment';

export const nicheCreatorLeaderboardQuery = gql`
  query NicheCreatorLeaderboardQuery ($nicheOid: String!, $input: RewardPeriodInput) {
    getNicheCreatorLeaderboard (nicheOid: $nicheOid, input: $input)
    @rest(
      type: "RewardLeaderboardUser",
      path: "/niches/{args.nicheOid}/creator-leaderboard?{args.input}"
    ) 
    {
      ...RewardLeaderboardUser
    }
  }
  ${RewardLeaderboardUserFragment}
`;
