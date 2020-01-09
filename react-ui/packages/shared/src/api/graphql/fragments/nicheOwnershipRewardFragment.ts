import gql from 'graphql-tag';
import { NrveUsdValueFragment } from './nrveUsdValueFragment';
import { NicheFragment } from './nicheFragment';

export const NicheOwnershipRewardFragment = gql`
  fragment NicheOwnershipReward on NicheOwnershipReward {
    niche @type(name: "Niche") {
      ...Niche
    }
    reward @type(name: "NrveUsdValue") {
      ...NrveUsdValue
    }
  }
  ${NicheFragment}
  ${NrveUsdValueFragment}
`;
