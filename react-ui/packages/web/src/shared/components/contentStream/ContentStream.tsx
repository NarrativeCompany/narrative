import * as React from 'react';
import {
  ContentStreamFilters,
  ContentStreamSortOrder,
  Post,
  WithContentStreamProps,
  withState,
  WithStateProps
} from '@narrative/shared';
import { ContentStreamSortOrderHelper } from '../../enhancedEnums/contentStreamSortOrder';
import { FormattedMessage } from 'react-intl';
import { Paragraph } from '../Paragraph';
import { FeaturedContentStreamRow, FeaturedContentStreamRowPosts } from './FeaturedContentStreamRow';
import { List } from 'antd';
import { ContentStreamItems } from './ContentStreamItems';
import { compose, withProps } from 'recompose';
import { generateSkeletonListProps, renderSkeleton } from '../../utils/loadingUtils';
import { ContentStreamMessages } from '../../i18n/ContentStreamMessages';
import { ContentStreamItemStyleProps } from './ContentStreamItem';

export interface ContentStreamProps extends WithContentStreamProps, ContentStreamItemStyleProps {
  sortOrder?: ContentStreamSortOrder;
  noResultsMessage: FormattedMessage.MessageDescriptor;
  banner?: React.ReactNode;
  featuredSplitPointCalculator?: (posts: Post[]) => number;
}

export const ContentStream: React.SFC<ContentStreamProps> = (props) => {
  const {
    sortOrder,
    noResultsMessage,
    posts,
    contentStreamLoading,
    loadMorePosts,
    loadMorePostsParams,
    banner,
    featuredSplitPointCalculator,
    ...itemStyleProps
  } = props;

  // jw: if we are rendering the first chunk of data, just output the skeletons straight.
  if (contentStreamLoading && !posts.length) {
    // jw: let's just show 5 placeholder records while we are waiting to load.
    return (
      <List {...generateSkeletonListProps(5, renderSkeleton)}/>
    );
  }

  // jw: if we don't have any results, then let's output that.
  if (!posts.length) {
    return (
      <Paragraph>
        <FormattedMessage {...noResultsMessage} />
      </Paragraph>
    );
  }

  const isFeatured = ContentStreamSortOrderHelper.isFeaturedType(sortOrder) || featuredSplitPointCalculator;

  let featuredPostRows: FeaturedContentStreamRowPosts[] | undefined;
  let standardPosts: Post[] = posts;
  if (isFeatured) {
    // jw: adding support for custom split point calculation
    let splitPoint = featuredSplitPointCalculator
      ? featuredSplitPointCalculator(posts)
      // bl: by default, we want up to 3 rows of 2 columns, so a max of 6 featured items
      : 6;

    // jw: Ensure a custom split calculator cannot specify a value beyond the length of the posts.
    splitPoint = Math.min(splitPoint, posts.length);

    // bl: we don't ever want an odd number here, so if it's odd, subtract 1
    if ((splitPoint % 2) === 1) {
      splitPoint = splitPoint - 1;
    }

    const featuredPosts = posts.slice(0, splitPoint);
    featuredPostRows = [];
    for (let i = 0; i < featuredPosts.length; i += 2) {
      featuredPostRows.push({post1: featuredPosts[i], post2: featuredPosts[i + 1]});
    }
    standardPosts = posts.slice(splitPoint);
  }

  return (
    <React.Fragment>
      {/* bl: show just the first row */}
      {featuredPostRows && featuredPostRows.length > 0 &&
        <FeaturedContentStreamRow
          {...featuredPostRows[0]}
          {...itemStyleProps}
        />
      }

      {/* bl: show the banner after the first row of featured posts now */}
      {banner}

      {/* bl: now show any remaining featured rows */}
      {featuredPostRows && featuredPostRows.length > 1 && featuredPostRows.slice(1).map((row, i) => (
        <FeaturedContentStreamRow key={i} {...row} {...itemStyleProps} />
      ))}

      {/* bl: finally, all of the standard posts */}
      {standardPosts && standardPosts.length > 0  &&
        <ContentStreamItems
          posts={standardPosts}
          loadMorePosts={loadMorePosts}
          loadMorePostsParams={loadMorePostsParams}
          {...itemStyleProps}
        />
      }
    </React.Fragment>
  );
};

// jw: just to make life easier, since we will be using this all over the place, let's create a utility to build the
//     props for us from the common inputs.

interface State {
  loadedSortOrder?: ContentStreamSortOrder;
}

export function withContentStreamPropsFromQuery(noResultsMessageOverride?: FormattedMessage.MessageDescriptor) {
  const noResultsMessage = noResultsMessageOverride ? noResultsMessageOverride : ContentStreamMessages.NoResultsMessage;

  return compose(
    // jw: this is tricky, but because apollo will serve data from a different sort order while transitioning, we need
    //     to track which sort order last loaded. For the initial load of the component let's treat the starting sort
    //     as loaded.
    // note: All of this should be able to be replaced with a much cleaner solution once this PR has been merged
    //       https://github.com/apollographql/react-apollo/pull/2889
    withState<State, ContentStreamFilters>((props) => {
      const loadedSortOrder = props.sortOrder;

      return { loadedSortOrder };
    }),
    // jw: to keep the transitioning properties clean, let's handle the sortOrder resolution in its own withProps
    withProps((props: WithContentStreamProps & ContentStreamFilters & WithStateProps<State>) => {
      const { sortOrder, contentStreamLoading, setState, state: { loadedSortOrder } } = props;

      const sortOrderHasChanged = sortOrder !== loadedSortOrder;

      // jw: if the content stream has loaded, then there is no need to clear the posts
      if (!contentStreamLoading) {
        // jw: but, if the loadedSortOrder does not match the one just resolved, then we need to update our state so
        //     that it does match.
        if (sortOrderHasChanged) {
          setState(ss => ({...ss, loadedSortOrder: sortOrder}));
        }

      // jw: if the sort order has changed and we are not loading, we don't want to display the cached posts.
      } else if (sortOrderHasChanged) {
        return { posts: [] };
      }

      // jw: guess everything is good to go, so no need to change anything. Leave the properties alone.
      return null;
    }),
    // jw: with all that sort craziness above taken care of, we should be good to generate the contentStreamProps
    withProps((props: WithContentStreamProps & ContentStreamFilters) => {
      const { contentStreamLoading, posts, loadMorePosts, loadMorePostsParams, sortOrder } = props;

      const contentStreamProps: ContentStreamProps =
        { contentStreamLoading, posts, loadMorePosts, loadMorePostsParams, sortOrder, noResultsMessage };

      return { contentStreamProps };
    })
  );
}

export interface WithContentStreamPropsFromQuery {
  contentStreamProps: ContentStreamProps;
}
