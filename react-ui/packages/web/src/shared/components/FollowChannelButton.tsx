import * as React from 'react';
import { compose, withHandlers, withProps } from 'recompose';
import {
  FollowChannelInput,
  withStartFollowingChannel,
  WithStartFollowingChannelProps,
  withStopFollowingChannel,
  WithStopFollowingChannelProps,
} from '@narrative/shared';
import { FollowButton, FollowButtonParentHandlers } from './FollowButton';
import { Channel } from '../utils/channelUtils';

export interface FollowChannelButtonProps {
  channel: Channel;
}

type Props =
  FollowChannelButtonProps &
  FollowButtonParentHandlers;

const FollowChannelButtonComponent: React.SFC<Props> = (props) => {
  const { channel, handleStartFollowing, handleStopFollowing } = props;

  return (
    <FollowButton
      followedItem={channel.currentUserFollowedItem}
      handleStartFollowing={handleStartFollowing}
      handleStopFollowing={handleStopFollowing}
    />
  );
};

type HandlersProps =
  WithStartFollowingChannelProps &
  WithStopFollowingChannelProps & {
    input: FollowChannelInput;
  };

export const FollowChannelButton = compose(
  // jw: first, we need the server callbacks for following a channel
  withStartFollowingChannel,
  withStopFollowingChannel,
  // jw: next: let's create the input object for the above API calls to make life easy.
  withProps((props: Props) => {
    const { channel } = props;
    const channelOid = channel.oid;
    const input: FollowChannelInput = { channelOid };

    return { input };
  }),
  // jw: finally, let's use these callbacks in handlers.
  withHandlers<HandlersProps, FollowButtonParentHandlers>({
    handleStartFollowing: (props) => async () => {
      const { input, startFollowingChannel } = props;

      await startFollowingChannel(input);
    },
    handleStopFollowing: (props) => async () => {
      const { input, stopFollowingChannel } = props;

      await stopFollowingChannel(input);
    }
  })
)(FollowChannelButtonComponent) as React.ComponentClass<FollowChannelButtonProps>;
