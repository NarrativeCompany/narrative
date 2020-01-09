import gql from 'graphql-tag';
import { UserFragment } from './userFragment';

export const ElectionNomineeFragment = gql`
  fragment ElectionNominee on ElectionNominee {
    oid
    nominee @type(name: "User") {
      ...User
    }
    status
    personalStatement
  }
  ${UserFragment}
`;
