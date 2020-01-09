import gql from 'graphql-tag';

export const PageInfoFragment = gql`
  fragment PageInfo on PageInfo {
    number
    size
    totalElements
    last
    totalPages
    numberOfElements
    first
  }
`;
