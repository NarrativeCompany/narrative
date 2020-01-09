import gql from 'graphql-tag';
import { SuspendEmailValidationFragment } from '../fragments/suspendEmailValidationFragment';

export const validateSuspendEmailAddressQuery = gql`
  query ValidateSuspendEmailAddressQuery ($input: SuspendEmailInput!, $userOid: String!) {
    validateSuspendEmailAddress (input: $input, userOid: $userOid)
    @rest(
      type: "SuspendEmailValidation",
      path: "/users/{args.userOid}/suspend-email-preference?{args.input}" 
    ){
      ...SuspendEmailValidation
    }
  }
  ${SuspendEmailValidationFragment}
`;
