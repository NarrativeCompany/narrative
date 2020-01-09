import gql from 'graphql-tag';
import { CommentFragment } from '../fragments/commentFragment';

// tslint:disable: max-line-length
export const qualityRateCommentMutation = gql`
  mutation QualityRateCommentMutation (
    $input: QualityRatingInput!, 
    $queryFields: CommentQueryFieldsInput!
  ) {
    qualityRateComment (input: $input, queryFields: $queryFields) 
    @rest(
      type: "Comment", 
      path: "/comments/{args.queryFields.consumerType}/{args.queryFields.consumerOid}/{args.queryFields.commentOid}/quality-rating", 
      method: "POST"
    ) {
        ...Comment
    }
  }
  ${CommentFragment}
`;
