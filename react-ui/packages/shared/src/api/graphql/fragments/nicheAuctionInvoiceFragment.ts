import gql from 'graphql-tag';
import { NicheAuctionFragment } from './nicheAuctionFragment';
import { NicheAuctionBidFragment } from './nicheAuctionBidFragment';

export const NicheAuctionInvoiceFragment = gql`
  fragment NicheAuctionInvoice on NicheAuctionInvoice {
    oid
    auction @type(name: "NicheAuction") {
      ...NicheAuction
    }
    bid @type(name: "NicheAuctionBid") {
      ...NicheAuctionBid
    }
  }
  ${NicheAuctionFragment}
  ${NicheAuctionBidFragment}
`;
