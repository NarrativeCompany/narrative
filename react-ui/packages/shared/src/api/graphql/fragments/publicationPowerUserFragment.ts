import gql from 'graphql-tag';

export const PublicationPowerUserFragment = gql`
  fragment PublicationPowerUser on PublicationPowerUser {
    oid

    roles
  }
`;
