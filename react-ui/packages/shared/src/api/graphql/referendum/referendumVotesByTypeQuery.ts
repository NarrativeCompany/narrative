import gql from 'graphql-tag';
import { ReferendumVoteGroupingFragment } from '../fragments/referendumVoteGroupingFragment';

export const referendumVotesByTypeQuery = gql`
  query ReferendumVotesByTypeQuery (
    $input: ReferendumVotesByTypeQueryInput!, 
    $referendumOid: String!, 
    $votedFor: Boolean!
  ) {
    getReferendumVotesByType (input: $input, referendumOid: $referendumOid, votedFor: $votedFor)
    @rest (
      type: "ReferendumVoteGrouping", 
      path: "/referendums/{args.referendumOid}/votes/{args.votedFor}?{args.input}"
    ) {
      ...ReferendumVoteGrouping
    }
  }
  ${ReferendumVoteGroupingFragment}
`;
