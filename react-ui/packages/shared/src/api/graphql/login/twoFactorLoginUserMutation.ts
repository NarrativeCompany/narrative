import gql from 'graphql-tag';
import { AuthDetailFragment } from '../fragments/authDetailFragment';

export const twoFactorLoginUserMutation = gql`
  mutation TwoFactorLoginMutation ($input: TwoFactorLoginInput!) {
    twoFactorLogin (input: $input) @rest(type: "AuthPayload", path: "/login/check-2fa", method: "POST") {
      ...AuthDetail
    }
  }
  ${AuthDetailFragment}
`;
