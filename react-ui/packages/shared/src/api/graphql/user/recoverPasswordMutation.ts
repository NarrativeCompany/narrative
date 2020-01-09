import gql from 'graphql-tag';

export const recoverPasswordMutation = gql`
  mutation RecoverPasswordMutation ($input: RecoverPasswordInput!) {
    recoverPassword (input: $input)
    @rest(
      type: "VoidResult",
      path: "/users/lost-password-email",
      method: "POST"
    ) {
      success
    }
  }
`;
