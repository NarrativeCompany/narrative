import gql from 'graphql-tag';

export const TopNicheFragment = gql`
  fragment TopNiche on TopNiche {
    oid
    name
    prettyUrlString
    totalPosts
  }
`;
