import { graphql, GraphqlQueryControls, NamedProps } from 'react-apollo';
import { commentForEditQuery } from '../graphql/comment/commentForEditQuery';
import { CommentForEditQuery, CommentForEditQueryVariables } from '../../types';
import { getCommentFields, WithCommentQueryProps } from './commentQueryConstants';

export type WithCommentForEditProps =
  NamedProps<{commentForEditData: GraphqlQueryControls & CommentForEditQuery}, WithCommentQueryProps>
  & WithCommentQueryProps;

export const withCommentForEdit =
  graphql<
    WithCommentQueryProps,
    CommentForEditQuery,
    CommentForEditQueryVariables,
    WithCommentForEditProps
>(commentForEditQuery, {
    options: (props: WithCommentQueryProps) => ({ variables: {
      queryFields: getCommentFields(props)
    }}),
    name: 'commentForEditData'
  });
