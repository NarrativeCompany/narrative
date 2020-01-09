import gql from 'graphql-tag';
import { PostDetailFragment } from '../fragments/postDetailFragment';

export const featurePostMutation = gql`
  mutation FeaturePostMutation ($input: FeaturedPostInput!, $postOid: String!) {
    featurePost (input: $input, postOid: $postOid) 
    @rest(type: "PostDetail", path: "/posts/{args.postOid}/featured", method: "PUT") {
      ...PostDetail
    }
  }
  ${PostDetailFragment}
`;
