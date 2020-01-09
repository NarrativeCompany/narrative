import gql from 'graphql-tag';
import { KycPricingFragment } from './kycPricingFragment';
import { PayPalCheckoutDetailsFragment } from './payPalCheckoutDetailsFragment';

export const UserKycFragment = gql`
  fragment UserKyc on UserKyc {
    oid
    kycStatus
    rejectedReasonEventType
  
    kycPricing @type(name: "KycPricing") {
      ...KycPricing
    }
    payPalCheckoutDetails @type(name: "PayPalCheckoutDetails") {
      ...PayPalCheckoutDetails
    }
  }
  ${KycPricingFragment}
  ${PayPalCheckoutDetailsFragment}
`;
