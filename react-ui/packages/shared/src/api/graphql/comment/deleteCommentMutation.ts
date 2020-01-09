import gql from 'graphql-tag';

export const deleteCommentMutation = gql`
  mutation DeleteCommentMutation ($input: CommentQueryFieldsInput!) {
    deleteComment (input: $input)
    @rest(
      type: "VoidResult",
      path: "/comments/{args.input.consumerType}/{args.input.consumerOid}/{args.input.commentOid}",
      method: "DELETE"
    ) {
        success
    }
  }
`;
