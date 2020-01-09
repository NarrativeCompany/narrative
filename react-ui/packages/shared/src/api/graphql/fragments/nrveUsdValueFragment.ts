import gql from 'graphql-tag';

export const NrveUsdValueFragment = gql`
  fragment NrveUsdValue on NrveUsdValue {
    nrve
    nrveRounded
    nrveDecimal
    usd
  }
`;
