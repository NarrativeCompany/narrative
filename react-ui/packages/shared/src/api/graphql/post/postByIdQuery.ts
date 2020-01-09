import gql from 'graphql-tag';
import { PostDetailFragment } from '../fragments/postDetailFragment';

export const postByIdQuery = gql`
  query PostByIdQuery ($postId: String!) {
    getPostById (postId: $postId) @rest (type: "PostDetail", path: "/posts/{args.postId}") {
      ...PostDetail
    }
  }
  ${PostDetailFragment}
`;
