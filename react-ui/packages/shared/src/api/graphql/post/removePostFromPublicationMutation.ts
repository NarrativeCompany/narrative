import gql from 'graphql-tag';
import { PostDetailFragment } from '../fragments/postDetailFragment';

export const removePostFromPublicationMutation = gql`
  mutation RemovePostFromPublicationMutation ($input: RemovePostFromPublicationInput!, $postOid: String!) {
    removePostFromPublication (input: $input, postOid: $postOid) 
    @rest(type: "PostDetail", path: "/posts/{args.postOid}/publication/delete", method: "POST") {
      ...PostDetail
    }
  }
  ${PostDetailFragment}
`;
