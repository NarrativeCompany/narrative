import gql from 'graphql-tag';
import { UserFragment } from './userFragment';

export const ReferendumVoteFragment = gql`
  fragment ReferendumVote on ReferendumVote {
    oid
    voteDatetime
    votedFor
    votePoints
    reason
    commentOid
    comment
    voter @type(name: "User") {
      ...User
    }
  }
  ${UserFragment}
`;
