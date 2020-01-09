import gql from 'graphql-tag';

export const resetPasswordMutation = gql`
  mutation ResetPasswordMutation ($input: ResetPasswordInput!, $userOid: String!) {
    resetPassword (input: $input, userOid: $userOid)
    @rest(
      type: "VoidResult",
      path: "/users/{args.userOid}/reset-password",
      method: "POST"
    ) {
      success
    }
  }
`;
