import * as React from 'react';
import { Omit } from 'recompose';
import { ChannelType, Niche } from '@narrative/shared';
import { ChannelLink, ChannelLinkProps } from '../channel/ChannelLink';

type Props = Omit<ChannelLinkProps, 'channel' | 'channelType'> & {
  niche?: Niche | null;
};

export const NicheLink: React.SFC<Props> = (props) => {
  const { niche, ...channelLinkProps } = props;

  return <ChannelLink channel={niche || undefined} channelType={ChannelType.NICHE} {...channelLinkProps}/>;
};
