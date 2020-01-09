import gql from 'graphql-tag';
import { UserFragment } from './userFragment';
import { CurrentUserFollowedItemFragment } from './currentUserFollowedItemFragment';

export const NicheFragment = gql`
  fragment Niche on Niche {
    oid
    type
    name
    description
    status
    prettyUrlString
    renewalDatetime
    currentUserFollowedItem @type(name: "CurrentUserFollowedItem") {
      ...CurrentUserFollowedItem
    }
    suggester @type(name: "User") {
      ...User
    }
    owner  @type(name: "User") {
      ...User
    }
  }
  ${UserFragment}
  ${CurrentUserFollowedItemFragment}
`;
