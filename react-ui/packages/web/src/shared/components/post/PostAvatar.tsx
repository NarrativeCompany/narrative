import * as React from 'react';
import { Omit } from 'recompose';
import { AvatarProps } from 'antd/lib/avatar';
import { Post } from '@narrative/shared';
import { Avatar } from 'antd';
import { PostLink } from './PostLink';

export interface PostAvatarProps extends Omit<AvatarProps, 'src'> {
  post: Post;
  link?: boolean;
  type?: 'square' | 'original';
}

export const PostAvatar: React.SFC<PostAvatarProps> = (props) => {
  const { post, link, type, alt, shape, ...avatarProps } = props;

  // jw: we will either have both image urls or neither, but because of Avatar.src typing, lets ensure both are set.
  if (!post.titleImageUrl || !post.titleImageSquareUrl) {
    return null;
  }

  const avatar = (
    <Avatar
      {...avatarProps}
      shape={shape || 'square'}
      src={type === 'original' ? post.titleImageUrl : post.titleImageSquareUrl}
      alt={alt || post.title}
    />
  );

  if (link === undefined || link) {
    return (
      <PostLink post={post} style={{display: 'block'}}>
        {avatar}
      </PostLink>
    );
  }

  return avatar;
};
