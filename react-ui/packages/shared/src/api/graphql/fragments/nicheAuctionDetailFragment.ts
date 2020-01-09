import gql from 'graphql-tag';
import { NicheAuctionFragment } from './nicheAuctionFragment';
import { PayPalCheckoutDetailsFragment } from './payPalCheckoutDetailsFragment';
import { NrveUsdValueFragment } from './nrveUsdValueFragment';

export const NicheAuctionDetailFragment = gql`
  fragment NicheAuctionDetail on NicheAuctionDetail {
    auction @type(name: "NicheAuction") {
      ...NicheAuction
    }
    currentUserLatestBidStatus
    currentUserLatestMaxNrveBid @type(name: "NrveUsdValue") {
      ...NrveUsdValue
    }
    currentUserActiveInvoiceOid
    currentUserBypassesSecurityDepositRequirement
    securityDepositPayPalCheckoutDetails @type(name: "PayPalCheckoutDetails") {
      ...PayPalCheckoutDetails
    }
  }
  ${NicheAuctionFragment}
  ${PayPalCheckoutDetailsFragment}
  ${NrveUsdValueFragment}
`;
