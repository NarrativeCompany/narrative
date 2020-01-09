import gql from 'graphql-tag';
import { UserReputationFragment } from './userReputationFragment';

export const UserFragment = gql`
  fragment User on User {
    oid
    displayName
    username
    avatarSquareUrl
    deleted
    labels
    reputation @type(name: "UserReputation") {
      ...UserReputation
    }
  }
  ${UserReputationFragment}
`;
