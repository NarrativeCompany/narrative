import { ChildDataProps, graphql, GraphqlQueryControls, NamedProps } from 'react-apollo';
import { moderatedPublicationPostsQuery } from '../graphql/publication/moderatedPublicationPostsQuery';
import { ExtractedPageableProps, Post, ModeratedPublicationPostsQuery } from '../../types';
import { getPageableQueryProps } from '../../utils';

const defaultPageSize: number = 30;
const queryName = 'moderatedPublicationPostsData';
const functionName = 'getModeratedPublicationPosts';

export interface WithModeratedPublicationPostsParentProps {
  publicationOid: string;
  currentPage?: number;
}

type WithProps = NamedProps<
  {[queryName]: GraphqlQueryControls & ModeratedPublicationPostsQuery},
  WithModeratedPublicationPostsProps
>;

export type WithModeratedPublicationPostsProps =
  ChildDataProps<WithModeratedPublicationPostsParentProps, ModeratedPublicationPostsQuery> &
  ExtractedPageableProps & {
  moderatedPosts: Post[];
};

export const withModeratedPublicationPosts =
  graphql<
    WithModeratedPublicationPostsParentProps,
    ModeratedPublicationPostsQuery,
    {},
    WithModeratedPublicationPostsProps
  >(moderatedPublicationPostsQuery, {
    options: ({publicationOid, currentPage}: WithModeratedPublicationPostsParentProps) => ({
      variables: {
        input: {
          size: defaultPageSize,
          page: currentPage  ? currentPage - 1 : 0
        },
        publicationOid
      }
    }),
    name: queryName,
    props: ({ moderatedPublicationPostsData, ownProps }: WithProps) => {
      const { items, ...extractedProps } =
        getPageableQueryProps<
          ModeratedPublicationPostsQuery,
          Post
        >(moderatedPublicationPostsData, functionName);

      return {
        ...ownProps,
        ...extractedProps,
        moderatedPosts: items,
        pageSize: defaultPageSize
      };
    }
});
