import gql from 'graphql-tag';
import { AuthDetailFragment } from './authDetailFragment';

export const VerifyEmailAddressResultFragment = gql`
  fragment VerifyEmailAddressResult on VerifyEmailAddressResult {
    emailAddress
    emailAddressToVerify
    incompleteVerificationSteps
    token @type(name: "AuthPayload") {
      ...AuthDetail
    }
  }
  ${AuthDetailFragment}
`;
