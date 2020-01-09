import gql from 'graphql-tag';
import { PayPalCheckoutDetailsFragment } from './payPalCheckoutDetailsFragment';

export const FiatPaymentFragment = gql`
  fragment FiatPayment on FiatPayment {
    oid
    nrveAmount
    transactionDate
    transactionId
    hasBeenPaid
    usdAmount
    feeUsdAmount
    status
    totalUsdAmount
    
    payPalCheckoutDetails @type(name: "PayPalCheckoutDetails") {
      ...PayPalCheckoutDetails
    }
  }
  ${PayPalCheckoutDetailsFragment}
`;
