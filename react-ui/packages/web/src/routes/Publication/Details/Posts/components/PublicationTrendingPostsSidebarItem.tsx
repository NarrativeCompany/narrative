import * as React from 'react';
import { branch, compose, renderComponent, withProps } from 'recompose';
import {
  ContentStreamChannelType,
  Publication,
  WithContentStreamProps,
  ContentStreamSortOrder,
  WithChannelContentStreamParentProps,
  withChannelContentStream
} from '@narrative/shared';
import { FormattedMessage } from 'react-intl';
import { PublicationDetailsMessages } from '../../../../../shared/i18n/PublicationDetailsMessages';
import { Paragraph } from '../../../../../shared/components/Paragraph';
import { PostLink } from '../../../../../shared/components/post/PostLink';
import { BasicCard } from '../../../../../shared/components/card/BasicCard';

interface ParentProps {
  publication: Publication;
}

const PublicationTrendingPostsSidebarItemComponent: React.SFC<WithContentStreamProps> = (props) => {
  // jw: the HOC stack will short out until we have results, so we are guaranteed now to have posts.
  const { posts } = props;

  return (
    <BasicCard title={<FormattedMessage {...PublicationDetailsMessages.TrendingPosts} />}>
      {posts.map((post) => (
        <Paragraph key={`trendingPost_${post.oid}`} marginBottom="small">
          <PostLink post={post} />
        </Paragraph>
      ))}
    </BasicCard>
  );
};

export const PublicationTrendingPostsSidebarItem = compose(
  withProps<WithChannelContentStreamParentProps, ParentProps>((props: ParentProps) => {
    const { oid } = props.publication;

    return {
      channelType: ContentStreamChannelType.publications,
      channelOid: oid,
      sortOrder: ContentStreamSortOrder.TRENDING,
      forWidget: true,
      count: 10
    };
  }),
  withChannelContentStream,
  // jw: until we load, and have results let's render nothing. No need including this sidebar item if there are no
  //     results.
  branch<WithContentStreamProps>(
    ({contentStreamLoading, posts}) => contentStreamLoading || !posts || posts.length === 0,
    renderComponent(() => null)
  )
)(PublicationTrendingPostsSidebarItemComponent) as React.ComponentClass<ParentProps>;
