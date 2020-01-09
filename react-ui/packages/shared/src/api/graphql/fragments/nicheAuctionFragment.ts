import gql from 'graphql-tag';
import { NicheFragment } from './nicheFragment';
import { NicheAuctionBidFragment } from './nicheAuctionBidFragment';
import { NrveUsdPriceFragment } from './nrveUsdPriceFragment';
import { NrveUsdValueFragment } from './nrveUsdValueFragment';

export const NicheAuctionFragment = gql`
  fragment NicheAuction on NicheAuction {
    oid
    openForBidding
    startDatetime
    endDatetime
    totalBidCount
    startingBid @type(name: "NrveUsdValue") {
      ...NrveUsdValue
    }
    nrveUsdPrice @type(name: "NrveUsdPrice") {
      ...NrveUsdPrice
    }
    currentRoleOutbid
    niche @type(name: "Niche") {
      ...Niche
    }
    leadingBid @type(name: "NicheAuctionBid") {
      ...NicheAuctionBid
    }
  }
  ${NicheFragment}
  ${NicheAuctionBidFragment}
  ${NrveUsdPriceFragment}
  ${NrveUsdValueFragment}
`;
