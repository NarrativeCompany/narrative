import * as React from 'react';
import { PostProps } from './MemberPostsListItem';
import { PostBulletedDetail } from './PostBulletedDetail';
import { FormattedMessage } from 'react-intl';
import { MemberPostsMessages } from '../../../shared/i18n/MemberPostsMessages';
import { MemberLink } from '../../../shared/components/user/MemberLink';
import { PublicationLink } from '../../../shared/components/publication/PublicationLink';

export const PostChannelBullet: React.SFC<PostProps> = (props) => {
  const { post: { author, publishedToPersonalJournal, publishedToPublication } } = props;

  // jw: if this is published to your journal, then it cannot be posted to any other primary channels.
  if (publishedToPersonalJournal) {
    return (
      <PostBulletedDetail>
        <FormattedMessage {...MemberPostsMessages.PostedToText}/>&nbsp;&nbsp;
        <MemberLink user={author} hideBadge={true}>
          <FormattedMessage {...MemberPostsMessages.Journal}/>
        </MemberLink>
      </PostBulletedDetail>
    );
  }

  // jw: if it was published to a publication we should link to that.
  if (publishedToPublication) {
    return (
      <PostBulletedDetail>
        <FormattedMessage {...MemberPostsMessages.PostedToText}/>&nbsp;&nbsp;
        <PublicationLink publication={publishedToPublication} />
      </PostBulletedDetail>
    );
  }

  return null;
};
