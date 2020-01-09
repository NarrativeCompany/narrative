import gql from 'graphql-tag';
import { UserFragment } from './userFragment';
import { CurrentUserFollowedItemFragment } from './currentUserFollowedItemFragment';

export const FollowedUserFragment = gql`
  fragment FollowedUser on FollowedUser {
    oid
    user @type(name: "User") {
      ...User
    }
    currentUserFollowedItem @type(name: "CurrentUserFollowedItem") {
      ...CurrentUserFollowedItem
    }
  }
  ${UserFragment}
  ${CurrentUserFollowedItemFragment}
`;
