import gql from 'graphql-tag';

export const authStateQuery = gql`
  query AuthStateQuery {
    authState @client {
      authenticationState
    }
  }
`;
