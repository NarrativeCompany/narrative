import gql from 'graphql-tag';

export const suspendEmailAddressForUserMutation = gql`
  mutation SuspendEmailAddressForUserMutation ($input: SuspendEmailInput!, $userOid: String!) {
    suspendEmailAddressForUser (input: $input, userOid: $userOid) 
    @rest(type: "VoidResult", path: "/users/{args.userOid}/suspend-email-preference", method: "PUT") {
      success
    }
  }
`;
