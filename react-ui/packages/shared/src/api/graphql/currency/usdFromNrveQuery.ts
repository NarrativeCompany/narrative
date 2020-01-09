import gql from 'graphql-tag';

export const usdFromNrveQuery = gql`
  query UsdFromNrveQuery ($input: UsdFromNrveInput!) {
    getUsdFromNrve (input: $input) @rest(type: "StringScalar", path: "/currencies/nrve-to-usd?{args.input}") {
      usdAmount: value
    }
  }
`;
