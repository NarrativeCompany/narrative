import { ChildDataProps, graphql, GraphqlQueryControls, NamedProps } from 'react-apollo';
import { commentsQuery } from '../graphql/comment/commentsQuery';
import { Comment, CommentsQuery, CommentsQueryVariables, ExtractedPageableProps } from '../../types';
import { getPageableQueryProps, infiniteLoadingFixProps, stripUndefinedProperties } from '../../utils';
import { ApolloQueryResult } from 'apollo-client';
import { getCompositionConsumerFields, WithCompositionConsumerFields } from './commentQueryConstants';

export const COMMENT_PAGE_SIZE = 25;
const queryName = 'commentsData';
const functionName = 'getComments';

export interface WithCommentsParentProps extends WithCompositionConsumerFields {
  currentPage: number;
  commentOid?: string;
  includeBuried?: boolean;
}

type WithProps = NamedProps<
  {[queryName]: GraphqlQueryControls & CommentsQuery},
  WithCommentsProps
>;

export type WithCommentsProps =
  ChildDataProps<WithCommentsParentProps, CommentsQuery> &
  ExtractedPageableProps & {
  comments: Comment[];
  buriedCommentCount: number | null;
  includeBuried?: boolean;
  currentPage: number;
  // tslint:disable-next-line no-any
  refetchComments: (variables?: CommentsQueryVariables) => Promise<ApolloQueryResult<any>>;
};

function getCommentsPageInput(props: WithCommentsParentProps) {
  const comment = props.commentOid;
  const size = COMMENT_PAGE_SIZE;

  // jw: if there is a commentOid provided, then this is a permalink and we do not need to specify a page or
  //     includeBuried flags since those will be derived from the comment.
  if (comment) {
    return { size, comment };
  }

  // jw: there is no reason to include the `includeBuried` prop for permalinks, but for pagination it is vital.
  // jw: we are defining it this way so that we will only pass it to the server if it is true.
  const includeBuried = props.includeBuried || undefined;

  // jw: the server uses indexes, while we are dealing with 1 based pages on the front end, so lets translate
  const page = props.currentPage - 1;

  // jw: otherwise, let's just use traditional paging
  // note: we don't want to send "includeBuried" unless it has a value.
  return stripUndefinedProperties({ size, page, includeBuried });
}

export const withComments =
  graphql<
    WithCommentsParentProps,
    CommentsQuery,
    CommentsQueryVariables,
    WithCommentsProps
  >(commentsQuery, {
    options: (ownProps: WithCommentsParentProps) => ({
      ...infiniteLoadingFixProps,
      variables: {
        queryFields: getCompositionConsumerFields(ownProps),
        pageInput: getCommentsPageInput(ownProps)
      }
    }),
    name: queryName,
    props: ({ commentsData, ownProps }: WithProps) => {
      const { refetch } = commentsData;
      const { items, ...extractedProps } =
        getPageableQueryProps<CommentsQuery, Comment>(commentsData, functionName);
      const result = commentsData && commentsData[functionName];
      const buriedCommentCount = result && result.buriedCommentCount;

      // jw: this could change as part of querying, so we need to get it if provided
      const includeBuried = result ? result.includeBuried || undefined : ownProps.includeBuried;

      // jw: let's get the current page from the results if possible, since this could change based on query needs.
      const currentPage = (result && result.info) ? result.info.number + 1 : ownProps.currentPage;

      return {
        ...ownProps,
        ...extractedProps,
        comments: items,
        pageSize: COMMENT_PAGE_SIZE,
        includeBuried,
        currentPage,
        refetchComments: refetch,
        buriedCommentCount
      };
    }
  });
