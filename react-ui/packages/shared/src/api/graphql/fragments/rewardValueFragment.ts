import gql from 'graphql-tag';

import { NrveUsdValueFragment } from './nrveUsdValueFragment';

export const RewardValueFragment = gql`
  fragment RewardValue on RewardValue {
    value @type(name: "NrveUsdValue") {
      ...NrveUsdValue
    }
  }
  ${NrveUsdValueFragment}
`;
