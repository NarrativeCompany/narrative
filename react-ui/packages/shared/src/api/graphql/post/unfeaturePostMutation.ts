import gql from 'graphql-tag';
import { PostDetailFragment } from '../fragments/postDetailFragment';

export const unfeaturePostMutation = gql`
  mutation UnfeaturePostMutation ($postOid: String!) {
    unfeaturePost (postOid: $postOid) 
    @rest(type: "PostDetail", path: "/posts/{args.postOid}/featured", method: "DELETE") {
      ...PostDetail
    }
  }
  ${PostDetailFragment}
`;
