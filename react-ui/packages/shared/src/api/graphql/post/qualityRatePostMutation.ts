import gql from 'graphql-tag';
import { PostDetailFragment } from '../fragments/postDetailFragment';

export const qualityRatePostMutation = gql`
  mutation QualityRatePostMutation ($input: QualityRatingInput!, $postOid: String!) {
    qualityRatePost (input: $input, postOid: $postOid) 
    @rest(type: "PostDetail", path: "/posts/{args.postOid}/quality-rating" method: "POST") {
      ...PostDetail
    }
  }
  ${PostDetailFragment}
`;
