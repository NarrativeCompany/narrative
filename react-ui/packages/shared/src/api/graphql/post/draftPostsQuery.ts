import gql from 'graphql-tag';
import { PostFragment } from '../fragments/postFragment';
import { PageInfoFragment } from '../fragments/pageInfoFragment';

export const draftPostsQuery = gql`
  query DraftPostsQuery ($input: PostListInput) {
    getDraftPosts (input: $input) @rest(type: "PostListPayload", path: "/users/current/draft-posts?{args.input}") {
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
