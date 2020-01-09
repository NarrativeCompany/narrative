import gql from 'graphql-tag';

export const PayPalCheckoutDetailsFragment = gql`
  fragment PayPalCheckoutDetails on PayPalCheckoutDetails {
    clientMode
    clientId
    amountForPayPal
    usdAmount
  }
`;
