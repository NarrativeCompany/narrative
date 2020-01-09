import gql from 'graphql-tag';
import { CurrentUserFollowedItemFragment } from '../fragments/currentUserFollowedItemFragment';

export const startFollowingChannelMutation = gql`
  mutation StartFollowingChannelMutation ($input: FollowChannelInput!) {
    startFollowingChannel (input: $input) @rest(
      type: "CurrentUserFollowedItem",
      path: "/channels/{args.input.channelOid}/followers",
      method: "POST"
    ) {
      ...CurrentUserFollowedItem
    }
  }
  ${CurrentUserFollowedItemFragment}
`;
