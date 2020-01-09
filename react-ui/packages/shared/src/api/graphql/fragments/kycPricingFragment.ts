import gql from 'graphql-tag';

export const KycPricingFragment = gql`
  fragment KycPricing on KycPricing {
    initialPrice
    retryPrice
    kycPromoPrice
    kycPromoMessage
  }
`;
