import gql from 'graphql-tag';
import { RewardValueFragment } from '../fragments/rewardValueFragment';

export const nicheAllTimeRewardsQuery = gql`
  query NicheAllTimeRewardsQuery ($nicheOid: String!) {
    getNicheAllTimeRewards (nicheOid: $nicheOid)
    @rest(type: "RewardValue", path: "/niches/{args.nicheOid}/all-time-rewards") {
      ...RewardValue
    }
  }
  ${RewardValueFragment}
`;
