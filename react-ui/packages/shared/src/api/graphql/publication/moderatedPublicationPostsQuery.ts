import gql from 'graphql-tag';
import { PostFragment } from '../fragments/postFragment';
import { PageInfoFragment } from '../fragments/pageInfoFragment';

export const moderatedPublicationPostsQuery = gql`
  query ModeratedPublicationPostsQuery($input: ModeratedPublicationPostsPageableInput!, $publicationOid: String!) {
    getModeratedPublicationPosts (input: $input, publicationOid: $publicationOid)
    @rest(
      type: "ModeratedPublicationPostsPayload", 
      path: "/publications/{args.publicationOid}/moderated-posts?{args.input}"
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
