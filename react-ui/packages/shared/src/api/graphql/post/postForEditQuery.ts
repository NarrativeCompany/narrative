import gql from 'graphql-tag';
import { EditPostDetailFragment } from '../fragments/editPostDetailFragment';

export const postForEditQuery = gql`
  query PostForEditQuery ($postOid: String!) {
    getPostForEdit (postOid: $postOid) @rest (type: "EditPostDetail", path: "/posts/{args.postOid}/edit-detail") {
      ...EditPostDetail
    }
  }
  ${EditPostDetailFragment}
`;
