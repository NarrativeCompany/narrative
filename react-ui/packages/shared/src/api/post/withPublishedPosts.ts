import { ChildDataProps, graphql, GraphqlQueryControls, NamedProps } from 'react-apollo';
import { publishedPostsQuery } from '../graphql/post/publishedPostsQuery';
import { ExtractedPageableProps, Post, PublishedPostsQuery, PublishedPostsQueryVariables } from '../../types';
import { getPageableQueryProps } from '../../utils';
import { ApolloQueryResult } from 'apollo-client';

const defaultPageSize = 50;
const queryName = 'publishedPostsData';
const functionName = 'getPublishedPosts';

interface ParentProps {
  currentPage: number;
}

type WithProps = NamedProps<
  {[queryName]: GraphqlQueryControls & PublishedPostsQuery},
  WithPublishedPostsProps
>;

export interface WithRefetchPostListHandler {
  // tslint:disable-next-line no-any
  refetchPostList?: (variables?: any) => Promise<ApolloQueryResult<any>>;
}

export interface WithPostsProps extends ExtractedPageableProps, WithRefetchPostListHandler {
  posts: Post[];
}

export type WithPublishedPostsProps =
  ChildDataProps<ParentProps, PublishedPostsQuery> &
  WithPostsProps &
  WithRefetchPostListHandler;

export const withPublishedPosts =
  graphql<
    ParentProps,
    PublishedPostsQuery,
    PublishedPostsQueryVariables,
    WithPublishedPostsProps
  >(publishedPostsQuery, {
    skip: (ownProps: ParentProps) => !ownProps.currentPage,
    options: ({currentPage}) => ({
      variables: {
        input: {
          size: defaultPageSize,
          page: currentPage - 1
        }
      }
    }),
    name: queryName,
    props: ({ publishedPostsData, ownProps }: WithProps) => {
      const { items, ...extractedProps } =
        getPageableQueryProps<PublishedPostsQuery, Post>(publishedPostsData, functionName);

      return {
        ...ownProps,
        ...extractedProps,
        posts: items,
        pageSize: defaultPageSize,
        refetchPostList: publishedPostsData && publishedPostsData.refetch,
      };
    }
  });
