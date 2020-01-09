import * as React from 'react';
import { Omit } from 'recompose';
import { AvatarProps } from 'antd/lib/avatar';
import { Publication } from '@narrative/shared';
import { Avatar } from 'antd';
import { PublicationLink } from './PublicationLink';

export interface PublicationAvatarProps extends Omit<AvatarProps, 'src'> {
  publication: Publication;
  link?: boolean;
}

export const PublicationAvatar: React.SFC<PublicationAvatarProps> = (props) => {
  const { publication, link, alt, shape, ...avatarProps } = props;

  // jw: if we do not have a logo then short out, since we do not have a default publication avatar.
  if (!publication.logoUrl) {
    return null;
  }

  const avatar = (
    <Avatar
      {...avatarProps}
      shape={shape || 'square'}
      src={publication.logoUrl}
      alt={alt || publication.name}
    />
  );

  if (link === undefined || link) {
    return (
      <PublicationLink publication={publication} style={{display: 'block'}}>
        {avatar}
      </PublicationLink>
    );
  }

  return avatar;
};
