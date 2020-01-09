import gql from 'graphql-tag';

export const NrveUsdPriceFragment = gql`
  fragment NrveUsdPrice on NrveUsdPrice {
    nrveUsdPrice
    expirationDatetime
    securityToken
  }
`;
