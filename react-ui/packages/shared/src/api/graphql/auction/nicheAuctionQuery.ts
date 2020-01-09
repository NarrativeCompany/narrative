import gql from 'graphql-tag';
import { NicheAuctionDetailFragment } from '../fragments/nicheAuctionDetailFragment';

export const nicheAuctionQuery = gql`
  query NicheAuctionQuery ($auctionOid: String!) {
    getNicheAuction (auctionOid: $auctionOid) @rest(
      type: "NicheAuctionDetail", 
      path: "/auctions/{args.auctionOid}",
      method: "GET"
    ) {
      ...NicheAuctionDetail
    }
  }
  ${NicheAuctionDetailFragment}
`;
