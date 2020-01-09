import gql from 'graphql-tag';

export const authStateMutation = gql`
  mutation AuthStateMutation ($input: AuthStateInput!) {
    updateAuthenticationState ( input: $input ) @client 
  }
`;
