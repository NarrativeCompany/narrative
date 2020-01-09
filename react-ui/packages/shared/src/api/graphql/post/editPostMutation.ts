import gql from 'graphql-tag';
import { EditPostDetailFragment } from '../fragments/editPostDetailFragment';

export const editPostMutation = gql`
  mutation EditPostMutation ($input: PostInput!, $postOid: String!) {
    editPost (input: $input, postOid: $postOid) 
    @rest(type: "EditPostDetail", path: "/posts/{args.postOid}" method: "POST") {
      ...EditPostDetail
    }
  }
  ${EditPostDetailFragment}
`;
