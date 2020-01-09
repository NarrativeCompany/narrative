import gql from 'graphql-tag';

export const commentForEditQuery = gql`
  query CommentForEditQuery ($queryFields: CommentQueryFieldsInput!) {
    getCommentForEdit (queryFields: $queryFields) 
    @rest(
      type: "CommentForEdit", 
      path: "/comments/{args.queryFields.consumerType}/{args.queryFields.consumerOid}/{args.queryFields.commentOid}"
    ) {
      value
    }
  }
`;
