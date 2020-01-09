import gql from 'graphql-tag';

export const resendCurrentUserVerificationEmailMutation = gql`
  mutation ResendCurrentUserVerificationEmailMutation ($input: EmptyInput) {
    resendCurrentUserVerificationEmail (input: $input)
    @rest(
      type: "VoidResult",
      path: "/users/current/email-verification-email",
      method: "POST"
    ) {
      success
    }
  }
`;
