import gql from 'graphql-tag';

export const RewardPeriodFragment = gql`
  fragment RewardPeriod on RewardPeriod {
    name
    yearMonth
  }
`;
