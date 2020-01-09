import gql from 'graphql-tag';
import { CommentFragment } from '../fragments/commentFragment';

export const postCommentMutation = gql`
  mutation PostCommentMutation ($input: CommentInput!, $queryFields: CompositionConsumerQueryFieldsInput!) {
    postComment (input: $input, queryFields: $queryFields) 
    @rest(
      type: "Comment", 
      path: "/comments/{args.queryFields.consumerType}/{args.queryFields.consumerOid}", 
      method: "POST"
    ) {
        ...Comment
    }
  }
  ${CommentFragment}
`;
