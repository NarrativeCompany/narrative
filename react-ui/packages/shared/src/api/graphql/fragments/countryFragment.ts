import gql from 'graphql-tag';

export const CountryFragment = gql`
  fragment Country on Country {
    countryCode
    countryName
  }
`;
