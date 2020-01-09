import gql from 'graphql-tag';
import { KycPricingFragment } from '../fragments/kycPricingFragment';

export const kycPricingQuery = gql`
  query KycPricingQuery {
    getKycPricing @rest(type: "KycPricing", path: "/kyc/pricing") {
      ...KycPricing
    }
  }
  ${KycPricingFragment}
`;
