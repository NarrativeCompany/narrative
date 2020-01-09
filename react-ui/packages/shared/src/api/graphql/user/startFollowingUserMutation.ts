import gql from 'graphql-tag';
import { CurrentUserFollowedItemFragment } from '../fragments/currentUserFollowedItemFragment';

export const startFollowingUserMutation = gql`
  mutation StartFollowingUserMutation ($input: FollowUserInput!) {
    startFollowingUser (input: $input) @rest(
      type: "CurrentUserFollowedItem",
      path: "/users/{args.input.userOid}/followers",
      method: "POST"
    ) {
      ...CurrentUserFollowedItem
    }
  }
  ${CurrentUserFollowedItemFragment}
`;
