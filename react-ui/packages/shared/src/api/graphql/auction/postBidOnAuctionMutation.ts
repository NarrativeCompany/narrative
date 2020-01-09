import gql from 'graphql-tag';
import { NicheAuctionDetailFragment } from '../fragments/nicheAuctionDetailFragment';

export const postBidOnAuctionMutation = gql`
  mutation PostBidOnAuctionMutation ($input: NicheAuctionBidInput!, $auctionOid: String!) {
    postBidOnAuction (input: $input, auctionOid: $auctionOid) @rest(
      type: "NicheAuctionDetail"
      path: "/auctions/{args.auctionOid}/bids", 
      method: "POST"
    ) {
      ...NicheAuctionDetail
    }
  }
  ${NicheAuctionDetailFragment}
`;
