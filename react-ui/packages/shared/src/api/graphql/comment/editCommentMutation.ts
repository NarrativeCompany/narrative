import gql from 'graphql-tag';
import { CommentFragment } from '../fragments/commentFragment';

export const editCommentMutation = gql`
  mutation EditCommentMutation (
    $input: CommentInput!, 
    $queryFields: CommentQueryFieldsInput!
  ) {
    editComment (input: $input, queryFields: $queryFields) 
    @rest(
      type: "Comment", 
      path: "/comments/{args.queryFields.consumerType}/{args.queryFields.consumerOid}/{args.queryFields.commentOid}", 
      method: "PUT"
    ) {
        ...Comment
    }
  }
  ${CommentFragment}
`;
