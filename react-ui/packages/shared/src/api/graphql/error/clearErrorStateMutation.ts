import gql from 'graphql-tag';

export const clearErrorStateMutation = gql`
  mutation ClearErrorStateMutation {
    clearErrorState @client
  }
`;
