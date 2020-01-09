import gql from 'graphql-tag';

export const setErrorStateMutation = gql`
  mutation SetErrorStateMutation ($input: ErrorStateInput!) {
    setErrorState ( input: $input ) @client 
  }
`;
