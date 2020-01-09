import gql from 'graphql-tag';
import { UserFragment } from './userFragment';

export const UserDetailFragment = gql`
  fragment UserDetail on UserDetail {
    user @type(name: "User") {
      ...User
    }
    joined
    lastVisit
    hideMyFollowers
    hideMyFollows
  }
  ${UserFragment}
`;
