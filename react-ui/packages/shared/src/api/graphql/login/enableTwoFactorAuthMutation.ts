import gql from 'graphql-tag';
import { AuthDetailFragment } from '../fragments/authDetailFragment';

export const enableTwoFactorAuthMutation = gql`
  mutation EnableTwoFactorAuthMutation ($input: EnableTwoFactorAuthInput!) {
    enableTwoFactorAuth (input: $input) @rest(type: "AuthPayload", path: "/users/current/2fa-secret", method: "POST") {
      ...AuthDetail
    }
  }
  ${AuthDetailFragment}
`;
