import gql from 'graphql-tag';

export const UserEmailAddressDetailFragment = gql`
  fragment UserEmailAddressDetail on UserEmailAddressDetail {
    oid
    emailAddress
    pendingEmailAddress
    pendingEmailAddressExpirationDatetime
    incompleteVerificationSteps
  }
`;
