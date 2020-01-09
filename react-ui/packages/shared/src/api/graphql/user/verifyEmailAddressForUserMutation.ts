import gql from 'graphql-tag';

export const verifyEmailAddressForUserMutation = gql`
  mutation VerifyEmailAddressForUserMutation ($input: VerifyEmailAddressInput!, $userOid: String!) {
    verifyEmailAddressForUser (input: $input, userOid: $userOid) 
    @rest(type: "VoidResult", path: "/users/{args.userOid}/email-verification", method: "PUT") {
      success
    }
  }
`;
