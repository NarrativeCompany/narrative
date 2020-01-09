import gql from 'graphql-tag';
import { CurrentUserFollowedItemFragment } from '../fragments/currentUserFollowedItemFragment';

export const stopFollowingUserMutation = gql`
  mutation StopFollowingUserMutation ($input: FollowUserInput!) {
    stopFollowingUser (input: $input) @rest(
      type: "CurrentUserFollowedItem",
      path: "/users/{args.input.userOid}/followers",
      method: "DELETE"
    ) {
      ...CurrentUserFollowedItem
    }
  }
  ${CurrentUserFollowedItemFragment}
`;
