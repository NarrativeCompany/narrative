import gql from 'graphql-tag';
import { VerifyEmailAddressResultFragment } from '../fragments/verifyEmailAddressResultFragment';

export const verifyPendingEmailAddressForUserMutation = gql`
  mutation VerifyPendingEmailAddressForUserMutation ($input: VerifyPendingEmailAddressInput!, $userOid: String!) {
    verifyPendingEmailAddressForUser (input: $input, userOid: $userOid) 
    @rest(type: "VerifyEmailAddressResult", path: "/users/{args.userOid}/pending-email-verification", method: "PUT") {
      ...VerifyEmailAddressResult
    }
  }
  ${VerifyEmailAddressResultFragment}
`;
