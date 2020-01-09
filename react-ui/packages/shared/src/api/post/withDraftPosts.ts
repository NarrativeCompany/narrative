import { ChildDataProps, graphql, GraphqlQueryControls, NamedProps } from 'react-apollo';
import { draftPostsQuery } from '../graphql/post/draftPostsQuery';
import { DraftPostsQuery, Post } from '../../types';
import { getPageableQueryProps } from '../../utils';
import { WithPostsProps } from './withPublishedPosts';

const defaultPageSize = 50;
const queryName = 'draftPostsData';
const functionName = 'getDraftPosts';

interface ParentProps {
  currentPage: number;
}

type WithProps = NamedProps<
  {[queryName]: GraphqlQueryControls & DraftPostsQuery},
  WithDraftPostsProps
>;

export type WithDraftPostsProps =
  ChildDataProps<ParentProps, DraftPostsQuery> &
  WithPostsProps;

export const withDraftPosts =
  graphql<
    ParentProps,
    DraftPostsQuery,
    {},
    WithDraftPostsProps
  >(draftPostsQuery, {
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
    props: ({ draftPostsData, ownProps }: WithProps) => {
      const { items, ...extractedProps } =
        getPageableQueryProps<DraftPostsQuery, Post>(draftPostsData, functionName);

      return {
        ...ownProps,
        ...extractedProps,
        posts: items,
        pageSize: defaultPageSize,
        refetchPostList: draftPostsData && draftPostsData.refetch,
      };
    }
  });
