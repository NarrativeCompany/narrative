import gql from 'graphql-tag';
import { PostFragment } from './postFragment';

export const PostDetailFragment = gql`
  fragment PostDetail on PostDetail {
    oid
    post @type(name: "Post") {
      ...Post
    }
    extract
    body
    canonicalUrl
    draft
    allowComments
    
    pendingPublicationApproval
    
    qualityRatingByCurrentUser
    ageRatingByCurrentUser
    
    editableByCurrentUser
    deletableByCurrentUser
  }
  ${PostFragment}
`;
