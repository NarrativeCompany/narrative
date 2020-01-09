import * as React from 'react';
import { FormattedMessage } from 'react-intl';
import { LocalizedTime } from '../../../shared/components/LocalizedTime';
import { MemberPostsMessages } from '../../../shared/i18n/MemberPostsMessages';
import { PostBulletedDetail } from './PostBulletedDetail';
import { PostProps } from './MemberPostsListItem';

export const PostDatetimeBullet: React.SFC<PostProps> = (props) => {
  const { post } = props;

  if (post.lastSaveDatetime) {
    return (
      <PostBulletedDetail>
        <FormattedMessage {...MemberPostsMessages.Saved}/>&nbsp;
        <LocalizedTime time={post.lastSaveDatetime} />
      </PostBulletedDetail>
    );
  }

  return (
    <PostBulletedDetail>
      <LocalizedTime time={post.liveDatetime} />
    </PostBulletedDetail>
  );
};
