import gql from 'graphql-tag';

export const PWResetURLValidationResultFragment = gql`
  fragment PWResetURLValidationResult on PWResetURLValidationResult {
    valid
    expired
    twoFactorEnabled
  }
`;
