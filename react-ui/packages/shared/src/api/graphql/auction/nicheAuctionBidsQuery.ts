import gql from 'graphql-tag';
import { NicheAuctionBidFragment } from '../fragments/nicheAuctionBidFragment';

export const nicheAuctionBidsQuery = gql`
  query NicheAuctionBidsQuery ($auctionOid: String!, $leadingBidOid: String) {
    getNicheAuctionBids (auctionOid: $auctionOid, leadingBidOid: $leadingBidOid) @rest(
      type: "NicheAuctionBid", 
      path: "/auctions/{args.auctionOid}/bids",
      method: "GET"
    ) {
      ...NicheAuctionBid
    }
  }
  ${NicheAuctionBidFragment}
`;
