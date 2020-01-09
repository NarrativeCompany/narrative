import gql from 'graphql-tag';
import { RewardPeriodFragment } from '../fragments/rewardPeriodFragment';

export const nicheRewardPeriodsQuery = gql`
  query NicheRewardPeriodsQuery ($nicheOid: String!) {
    getNicheRewardPeriods (nicheOid: $nicheOid)
    @rest(
      type: "RewardPeriod",
      path: "/niches/{args.nicheOid}/reward-periods"
    ) 
    {
      ...RewardPeriod
    }
  }
  ${RewardPeriodFragment}
`;
