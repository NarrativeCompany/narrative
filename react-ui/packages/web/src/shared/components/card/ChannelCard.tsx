import * as React from 'react';
import { DeletedChannel } from '@narrative/shared';
import { ChannelLink } from '../channel/ChannelLink';
import { FollowChannelButton } from '../FollowChannelButton';
import { Channel } from '../../utils/channelUtils';
import { BasicCard, BasicCardProps } from './BasicCard';
import { Omit } from 'recompose';
import { ChannelStatusTag } from '../channel/ChannelStatusTag';
import { FormattedMessage } from 'react-intl';
import { EnhancedChannelType } from '../../enhancedEnums/channelType';
import { MemberLink } from '../user/MemberLink';

interface ChannelCardProps extends Omit<BasicCardProps, 'title' | 'afterTitle' | 'footer'> {
  channel?: Channel;
  deletedChannel?: DeletedChannel;
  link?: boolean;
  includeStatus?: boolean;
}

export const ChannelCard: React.SFC<ChannelCardProps> = (props) => {
  const {
    channel,
    deletedChannel,
    link,
    includeStatus,
    ...basicCardProps
  } = props;

  const channelResolved = channel || deletedChannel;

  let name;
  if (deletedChannel) {
    name = deletedChannel.name;
  } else if (channel) {
    name = link === undefined || link
      ? <ChannelLink channel={channel} size="inherit" color="inherit" />
      : channel.name;
  }

  let description;
  if (deletedChannel) {
    if (deletedChannel.owner) {
      const owner = <MemberLink user={deletedChannel.owner} />;
      const enhancedChannelType = EnhancedChannelType.get(deletedChannel.type);
      description = <FormattedMessage {...enhancedChannelType.ownerMessage} values={{owner}} />;
    }
  } else if (channel) {
    description = channel.description;
  }

  const afterTitle = includeStatus && channelResolved &&
    <ChannelStatusTag channel={channelResolved} marginLeft="small" />;

  return (
    <BasicCard
      title={name}
      afterTitle={afterTitle}
      footer={channel && <FollowChannelButton channel={channel}/>}
      {...basicCardProps}>
      {description}
    </BasicCard>
  );
};
