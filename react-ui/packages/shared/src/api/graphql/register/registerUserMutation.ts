import gql from 'graphql-tag';
import { UserFragment } from '../fragments/userFragment';

export const registerUserMutation = gql`
  mutation RegisterUserMutation ($input: RegisterUserInput!) {
    registerUser (input: $input) @rest(type: "User", path: "/users", method: "POST") {
      ...User
    }
  }
  ${UserFragment}
`;
