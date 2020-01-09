import gql from 'graphql-tag';
import { ReferendumVoteFragment } from './referendumVoteFragment';
import { NicheFragment } from './nicheFragment';
import { PublicationFragment } from './publicationFragment';
import { DeletedChannelFragment } from './deletedChannelFragment';

export const ReferendumFragment = gql`
  fragment Referendum on Referendum {
    oid
    type
    startDatetime
    endDatetime
    votePointsFor
    votePointsAgainst
    commentCount
    open
    currentUserVote @type(name: "ReferendumVote") {
      ...ReferendumVote
    }
    niche @type(name: "Niche") {
      ...Niche
    }
    publication @type(name: "Publication") {
      ...Publication
    }
    deletedChannel @type(name: "DeletedChannel") {
      ...DeletedChannel
    }
  }
  ${ReferendumVoteFragment}
  ${NicheFragment}
  ${PublicationFragment}
  ${DeletedChannelFragment}
`;
