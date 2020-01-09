import gql from 'graphql-tag';
import { UserFragment } from './userFragment';
import { QualityRatingFieldsFragment } from './qualityRatingFieldsFragment';

export const CommentFragment = gql`
  fragment Comment on Comment {
    oid
    body
    user @type(name: "User") {
      ...User
    }
    liveDatetime
    qualityRatingFields @type(name: "QualityRatingFields") {
      ...QualityRatingFields
    }
    qualityRatingByCurrentUser
  }
  ${UserFragment}
  ${QualityRatingFieldsFragment}
`;
