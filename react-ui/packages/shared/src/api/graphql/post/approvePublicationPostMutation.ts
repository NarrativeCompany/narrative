import gql from 'graphql-tag';
import { PostDetailFragment } from '../fragments/postDetailFragment';

export const approvePublicationPostMutation = gql`
  mutation ApprovePublicationPostMutation ($input: ApprovePublicationPostInput!) {
    approvePublicationPost (input: $input) 
    @rest(type: "PostDetail", path: "/posts/{args.input.postOid}/publication", method: "PUT") {
      ...PostDetail
    }
  }
  ${PostDetailFragment}
`;
