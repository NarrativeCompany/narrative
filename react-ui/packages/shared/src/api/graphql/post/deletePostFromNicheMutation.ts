import gql from 'graphql-tag';
import { PostDetailFragment } from '../fragments/postDetailFragment';

export const deletePostFromNicheMutation = gql`
  mutation DeletePostFromNicheMutation ($postOid: String!, $nicheOid: String!) {
    deletePostFromNiche (postOid: $postOid, nicheOid: $nicheOid) 
    @rest(type: "PostDetail", path: "/posts/{args.postOid}/niches/{args.nicheOid}", method: "DELETE") {
      ...PostDetail
    }
  }
  ${PostDetailFragment}
`;
