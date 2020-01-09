import * as React from 'react';
import { Omit } from 'recompose';
import { ChannelType, Publication } from '@narrative/shared';
import { ChannelLink, ChannelLinkProps } from '../channel/ChannelLink';

type Props = Omit<ChannelLinkProps, 'channel' | 'channelType'> & {
  publication?: Publication | null;
};

export const PublicationLink: React.SFC<Props> = (props) => {
  const { publication, color, ...channelLinkProps } = props;

  return (
    <ChannelLink
      channel={publication || undefined}
      channelType={ChannelType.PUBLICATION}
      color={color || 'default'}
      {...channelLinkProps}
    />
  );
};
