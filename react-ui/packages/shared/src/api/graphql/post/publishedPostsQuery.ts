import gql from 'graphql-tag';
import { PostFragment } from '../fragments/postFragment';
import { PageInfoFragment } from '../fragments/pageInfoFragment';

export const publishedPostsQuery = gql`
  query PublishedPostsQuery ($input: PostListInput) {
    getPublishedPosts (input: $input)
    @rest(type: "PostListPayload", path: "/users/current/published-posts?{args.input}") {
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
