import gql from 'graphql-tag';
import { RewardPeriodFragment } from '../fragments/rewardPeriodFragment';

export const rewardPeriodsQuery = gql`
  query RewardPeriodsQuery {
    getRewardPeriods @rest(type: "RewardPeriod", path: "/rewards/completed-periods") {
      ...RewardPeriod
    }
  }
  ${RewardPeriodFragment}
`;
