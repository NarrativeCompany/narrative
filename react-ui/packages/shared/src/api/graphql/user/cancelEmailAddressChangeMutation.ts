import gql from 'graphql-tag';

export const cancelEmailAddressChangeMutation = gql`
  mutation CancelEmailAddressChangeMutation ($input: VerifyPendingEmailAddressInput!, $userOid: String!) {
    cancelEmailAddressChange (input: $input, userOid: $userOid) 
    @rest(
      type: "VoidResult", 
      path: "/users/{args.userOid}/pending-email-verification/cancel", 
      method: "POST"
    ) {
      emailAddress: value
    }
  }
`;
