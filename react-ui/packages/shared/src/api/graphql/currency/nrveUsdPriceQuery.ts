import gql from 'graphql-tag';
import { NrveUsdPriceFragment } from '../fragments/nrveUsdPriceFragment.js';

export const nrveUsdPriceQuery = gql`
  query NrveUsdPriceQuery {
    getNrveUsdPrice @rest(type: "NrveUsdPrice", path: "/currencies/nrve-to-usd/price") {
      ...NrveUsdPrice
    }
  }
  ${NrveUsdPriceFragment}
`;
