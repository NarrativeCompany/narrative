import gql from 'graphql-tag';

export const disableTwoFactorAuthMutation = gql`
  mutation DisableTwoFactorAuthMutation ($input: DisableTwoFactorAuthInput!) {
    disableTwoFactorAuth (input: $input) @rest(type: "VoidResult", path: "/users/current/2fa-secret", method: "PUT") {
      success
    }
  }
`;
