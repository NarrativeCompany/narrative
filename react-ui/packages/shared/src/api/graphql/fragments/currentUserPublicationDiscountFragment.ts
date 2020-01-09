import gql from 'graphql-tag';

export const PublicationDiscountFragment = gql`
  fragment PublicationDiscount on PublicationDiscount {
    oid
    eligibleForDiscount
  }
`;
