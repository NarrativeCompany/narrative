import gql from 'graphql-tag';
import { ReferendumVoteGroupingFragment } from './referendumVoteGroupingFragment';

export const ReferendumVotesFragment = gql`
  fragment ReferendumVotes on ReferendumVotes {
    totalVotes
    votePointsFor
    votePointsAgainst
    recentVotesFor @type(name: "ReferendumVoteGrouping") {
      ...ReferendumVoteGrouping
    }
    recentVotesAgainst @type(name: "ReferendumVoteGrouping") {
      ...ReferendumVoteGrouping
    }
    tribunalMembersYetToVote @type(name: "ReferendumVoteGrouping") {
      ...ReferendumVoteGrouping
    }
  }
  ${ReferendumVoteGroupingFragment}
`;
