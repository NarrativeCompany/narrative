import gql from 'graphql-tag';
import { RewardLeaderboardPostFragment } from '../fragments/rewardLeaderboardPostFragment';

export const nichePostLeaderboardQuery = gql`
  query NichePostLeaderboardQuery ($nicheOid: String!, $input: RewardPeriodInput) {
    getNichePostLeaderboard (nicheOid: $nicheOid, input: $input)
    @rest(
      type: "RewardLeaderboardPost",
      path: "/niches/{args.nicheOid}/post-leaderboard?{args.input}"
    ) 
    {
      ...RewardLeaderboardPost
    }
  }
  ${RewardLeaderboardPostFragment}
`;
