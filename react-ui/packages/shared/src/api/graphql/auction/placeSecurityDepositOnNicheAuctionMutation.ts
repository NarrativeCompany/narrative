import gql from 'graphql-tag';
import { NicheAuctionDetailFragment } from '../fragments/nicheAuctionDetailFragment';

export const placeSecurityDepositOnNicheAuctionMutation = gql`
  mutation PlaceSecurityDepositOnNicheAuctionMutation ($input: FiatPaymentInput!, $auctionOid: String!) {
    placeSecurityDepositOnNicheAuction (input: $input, auctionOid: $auctionOid) @rest(
      type: "NicheAuctionDetail"
      path: "/auctions/{args.auctionOid}/security-deposit", 
      method: "POST"
    ) {
      ...NicheAuctionDetail
    }
  }
  ${NicheAuctionDetailFragment}
`;
