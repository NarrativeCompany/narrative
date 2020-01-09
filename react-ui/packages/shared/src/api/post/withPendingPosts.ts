import { ChildDataProps, graphql, GraphqlQueryControls, NamedProps } from 'react-apollo';
import { pendingPostsQuery } from '../graphql/post/pendingPostsQuery';
import { PendingPostsQuery, Post } from '../../types';
import { getPageableQueryProps } from '../../utils';
import { WithPostsProps } from './withPublishedPosts';

const defaultPageSize = 50;
const queryName = 'pendingPostsData';
const functionName = 'getPendingPosts';

interface ParentProps {
  currentPage: number;
}

type WithProps = NamedProps<
  {[queryName]: GraphqlQueryControls & PendingPostsQuery},
  WithPendingPostsProps
>;

export type WithPendingPostsProps =
  ChildDataProps<ParentProps, PendingPostsQuery> &
  WithPostsProps;

export const withPendingPosts =
  graphql<
    ParentProps,
    PendingPostsQuery,
    {},
    WithPendingPostsProps
  >(pendingPostsQuery, {
    skip: (ownProps: ParentProps) => !ownProps.currentPage,
    options: (ownProps: ParentProps) => ({
      variables: {
        input: {
          size: defaultPageSize,
          page: ownProps.currentPage - 1
        }
      }
    }),
    name: queryName,
    props: ({ pendingPostsData, ownProps }: WithProps) => {
      const { items, ...extractedProps } =
        getPageableQueryProps<PendingPostsQuery, Post>(pendingPostsData, functionName);

      return {
        ...ownProps,
        ...extractedProps,
        posts: items,
        pageSize: defaultPageSize,
        refetchPostList: pendingPostsData && pendingPostsData.refetch,
      };
    }
  });
