import * as React from 'react';
import { WithModeratedPublicationPostsProps } from '@narrative/shared';
import { PublicationDetailsMessages } from '../../../../../shared/i18n/PublicationDetailsMessages';
import { FormattedMessage } from 'react-intl';
import { Paragraph } from '../../../../../shared/components/Paragraph';
import { ContentStreamItemsContainer } from '../../../../../shared/components/contentStream/ContentStreamItems';
import { ContentStreamItem } from '../../../../../shared/components/contentStream/ContentStreamItem';

type Props = Pick<WithModeratedPublicationPostsProps, 'moderatedPosts'>;

export const ReviewQueueModeratedPosts: React.SFC<Props> = (props) => {
  const { moderatedPosts } = props;

  if (!moderatedPosts.length) {
    return (
      <Paragraph>
        <FormattedMessage {...PublicationDetailsMessages.NoModeratedPostsToShow}/>
      </Paragraph>
    );
  }

  return (
    <ContentStreamItemsContainer>
      {moderatedPosts.map((post) => <ContentStreamItem key={post.oid} post={post} forPublicationReview={true} />)}
    </ContentStreamItemsContainer>
  );
};
