import * as React from 'react';
import { compose, Omit, withProps } from 'recompose';
import { isPrerenderUserAgent, Post, WithContentStreamProps } from '@narrative/shared';
import {
  withLoadMoreButtonController, WithLoadMoreButtonControllerParentProps, WithLoadMoreButtonControllerProps
} from '../../containers/withLoadMoreButtonController';
import { LoadMoreButton } from '../LoadMoreButton';
import { ContentStreamItem, ContentStreamItemStyleProps } from './ContentStreamItem';
import styled from '../../styled';
import { mediaQuery } from '../../styled/utils/mediaQuery';
import { RouterProps, withRouter } from 'react-router';
import { createUrl } from '../../utils/routeUtils';

export const ContentStreamItemsContainer = styled.div`
  ${mediaQuery.lg_up`
    .ant-card {
      background: transparent;
      border-color: transparent;
      
      &:not(:first-child) {
        border-top: 1px solid ${p => p.theme.borderGrey};
        padding-top: 20px;
      }
    }
  `}
`;

type ParentProps = Omit<WithContentStreamProps, 'contentStreamLoading'>;

type Props =
  Omit<ParentProps, 'loadMorePosts'> &
  ContentStreamItemStyleProps &
  WithLoadMoreButtonControllerProps;

const ContentStreamItemsComponent: React.SFC<Props> = (props) => {
  const { posts, loadMoreButtonProps, ...itemStyleProps } = props;

  if (!posts.length) {
    // todo:error-reporting: Why were we called if there are no posts to display?
    return null;
  }

  return (
    <ContentStreamItemsContainer>
      {posts.map((post: Post) => <ContentStreamItem key={post.oid} post={post} {...itemStyleProps} />)}

      {loadMoreButtonProps && <LoadMoreButton {...loadMoreButtonProps}/>}
    </ContentStreamItemsContainer>
  );
};

export const ContentStreamItems = compose(
  withRouter,
  withProps<WithLoadMoreButtonControllerParentProps, WithContentStreamProps & RouterProps>((props) => {
    const { history: { location } } = props;

    // jw: we need to convert the loadMorePosts property into the name that the load more controller expects.
    const loadMore = props.loadMorePosts;
    const loadMoreParams = props.loadMorePostsParams;

    // jw: if we have loadMoreParams and this is for a prerender agent then let's go ahead and include a loadMoreUrl
    let loadMoreUrl;
    if (loadMoreParams && isPrerenderUserAgent()) {
      // jw: note: there is a hidden assumption here that the content stream will never be using query args of its own
      loadMoreUrl = createUrl(location.pathname, loadMoreParams);
    }

    return { loadMore, loadMoreUrl };
  }),
  withLoadMoreButtonController
)(ContentStreamItemsComponent) as React.ComponentClass<ParentProps>;
