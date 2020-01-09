import gql from 'graphql-tag';

export const deleteCurrentUserMutation = gql`
  mutation DeleteCurrentUserMutation ($input: DeleteCurrentUserInput!) {
    deleteCurrentUser (input: $input) @rest(type: "VoidResult", path: "/users/current/delete", method: "POST") {
      success
    }
  }
`;
