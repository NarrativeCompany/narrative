import gql from 'graphql-tag';
import { UserFragment } from './userFragment';
import { NrveUsdValueFragment } from './nrveUsdValueFragment';

export const NicheAuctionBidFragment = gql`
  fragment NicheAuctionBid on NicheAuctionBid {
    oid
    status
    bidAmount @type(name: "NrveUsdValue") {
      ...NrveUsdValue
    }
    bidDatetime
    bidder @type(name: "User") {
      ...User
    }
  }
  ${UserFragment}
  ${NrveUsdValueFragment}
`;
