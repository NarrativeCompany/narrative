import gql from 'graphql-tag';
import { CurrentUserFollowedItemFragment } from '../fragments/currentUserFollowedItemFragment';

export const currentUserFollowingUserQuery = gql`
  query CurrentUserFollowingUserQuery ($userOid: String!) {
    getCurrentUserFollowingUser (userOid: $userOid) @rest(
      type: "CurrentUserFollowedItem",
      path: "/users/{args.userOid}/followers/current",
      method: "GET"
    ) {
      ...CurrentUserFollowedItem
    }
  }
  ${CurrentUserFollowedItemFragment}
`;
