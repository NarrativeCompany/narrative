import gql from 'graphql-tag';
import { UserFragment } from './userFragment';

export const SuspendEmailValidationFragment = gql`
  fragment SuspendEmailValidation on SuspendEmailValidation {
    error
    user @type(name: "User") {
      ...User
    }
  }
  ${UserFragment}
`;
