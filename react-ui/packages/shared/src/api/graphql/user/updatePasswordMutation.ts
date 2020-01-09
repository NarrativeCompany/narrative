import gql from 'graphql-tag';
import { AuthDetailFragment } from '../fragments/authDetailFragment';

export const updatePasswordMutation = gql`
  mutation UpdatePasswordMutation ($input: UpdatePasswordInput!) {
    updatePassword (input: $input) @rest(type: "AuthPayload", path: "/users/current/password", method: "PUT") {
      ...AuthDetail
    }
  }
  ${AuthDetailFragment}
`;
