import gql from 'graphql-tag';
import { CurrentUserFollowedItemFragment } from '../fragments/currentUserFollowedItemFragment';

export const stopFollowingChannelMutation = gql`
  mutation StopFollowingChannelMutation ($input: FollowChannelInput!) {
    stopFollowingChannel (input: $input) @rest(
      type: "CurrentUserFollowedItem",
      path: "/channels/{args.input.channelOid}/followers",
      method: "DELETE"
    ) {
      ...CurrentUserFollowedItem
    }
  }
  ${CurrentUserFollowedItemFragment}
`;
