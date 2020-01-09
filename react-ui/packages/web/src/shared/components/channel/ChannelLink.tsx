import * as React from 'react';
import { FormattedMessage } from 'react-intl';
import { ChannelType } from '@narrative/shared';
import { Link, LinkProps, LinkStyleProps } from '../Link';
import { Channel, getChannelUrl } from '../../utils/channelUtils';
import { ChannelMessages } from '../../i18n/ChannelMessages';
import { EnhancedChannelType } from '../../enhancedEnums/channelType';

export interface ChannelLinkProps extends LinkStyleProps, Pick<LinkProps, 'itemProp'> {
  channel?: Channel;
  channelType?: ChannelType;
  linkPath?: string;
}

export const ChannelLink: React.SFC<ChannelLinkProps> = (props) => {
  const { channel, channelType, color, linkPath, ...linkProps } = props;

  if (!channel) {
    if (channelType) {
      const enhancedType = EnhancedChannelType.get(channelType);
      return <FormattedMessage {...enhancedType.deletedChannelMessage}/>;
    }
    return <FormattedMessage {...ChannelMessages.DeletedChannel}/>;
  }

  const channelUrl = getChannelUrl(channel);

  // jw: if content was specified, then let's use that over the default channel name
  return (
    <Link
      {...linkProps}
      color={color || 'dark'}
      to={linkPath || channelUrl}
    >
      {props.children ? props.children : channel.name}
    </Link>
  );
};
