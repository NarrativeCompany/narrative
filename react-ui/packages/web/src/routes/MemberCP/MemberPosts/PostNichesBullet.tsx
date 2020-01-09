import * as React from 'react';
import { FormattedMessage } from 'react-intl';
import { MemberPostsMessages } from '../../../shared/i18n/MemberPostsMessages';
import { NicheLink } from '../../../shared/components/niche/NicheLink';
import { PostProps } from './MemberPostsListItem';
import { PostBulletedDetail } from './PostBulletedDetail';

export const PostNichesBullet: React.SFC<PostProps> = (props) => {
  const { post } = props;

  const publishedToNiches = post.publishedToNiches || [];

  // jw: if this post was not published to any niches then let's just short out.
  if (!publishedToNiches.length) {
    return null;
  }

  return (
    <PostBulletedDetail>
      <FormattedMessage {...MemberPostsMessages.Niches}/>&nbsp;
      {publishedToNiches.map((niche, i) => {
        return (
          <span key={niche.oid}>
            {i === 0 ? '' : ','}&nbsp;
            <NicheLink niche={niche} color="default"/>
          </span>
        );
      })}
    </PostBulletedDetail>
  );
};
