import gql from 'graphql-tag';
import { PostFragment } from '../fragments/postFragment';
import { PageInfoFragment } from '../fragments/pageInfoFragment';

export const pendingPostsQuery = gql`
  query PendingPostsQuery ($input: PostListInput) {
    getPendingPosts (input: $input) @rest(
      type: "PostListPayload", 
      path: "/users/current/pending-posts?{args.input}"
    ) {
      items @type(name: "Post") {
        ...Post
      }
      info @type(name: "PageInfo") {
        ...PageInfo
      }
    }
  }
  ${PostFragment}
  ${PageInfoFragment}
`;
