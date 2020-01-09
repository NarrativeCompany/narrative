import gql from 'graphql-tag';
import { ReferendumVoteFragment } from './referendumVoteFragment';

export const ReferendumVoteGroupingFragment = gql`
  fragment ReferendumVoteGrouping on ReferendumVoteGrouping {
    votedFor
    hasMoreItems
    lastVoterDisplayName
    lastVoterUsername
    items @type(name: "ReferendumVote") {
      ...ReferendumVote
    }
  }
  ${ReferendumVoteFragment}
`;
