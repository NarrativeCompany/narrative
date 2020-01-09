import gql from 'graphql-tag';

export const PermissionFragment = gql`
  fragment Permission on Permission {
    granted
  }
`;
