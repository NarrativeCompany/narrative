import gql from 'graphql-tag';
import { PWResetURLValidationResultFragment } from '../fragments/pWResetURLValidationResultFragment';

export const validateResetPasswordUrlQuery = gql`
  query ValidateResetPasswordUrlQuery ($input: ValidateResetPasswordUrlInput!, $userOid: String!) {
    validateResetPasswordUrl (input: $input, userOid: $userOid)
    @rest(
      type: "PWResetURLValidationResult",
      path: "/users/{args.userOid}/reset-password?{args.input}" 
    ){
      ...PWResetURLValidationResult
    }
  }
  ${PWResetURLValidationResultFragment}
`;
