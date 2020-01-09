import gql from 'graphql-tag';
import { EditPostDetailFragment } from '../fragments/editPostDetailFragment';

export const submitPostMutation = gql`
  mutation SubmitPostMutation ($input: PostInput!) {
    submitPost (input: $input) @rest(type: "EditPostDetail", path: "/posts" method: "POST") {
      ...EditPostDetail
    }
  }
  ${EditPostDetailFragment}
`;
