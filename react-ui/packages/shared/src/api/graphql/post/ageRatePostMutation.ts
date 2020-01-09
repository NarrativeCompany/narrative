import gql from 'graphql-tag';
import { PostDetailFragment } from '../fragments/postDetailFragment';

export const ageRatePostMutation = gql`
  mutation AgeRatePostMutation ($input: AgeRatingInput!, $postOid: String!) {
    ageRatePost (input: $input, postOid: $postOid) 
    @rest(type: "PostDetail", path: "/posts/{args.postOid}/age-rating" method: "POST") {
      ...PostDetail
    }
  }
  ${PostDetailFragment}
`;
