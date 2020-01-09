import gql from 'graphql-tag';
import { ReferendumVotesFragment } from '../fragments/referendumVotesFragment';

// TODO: uniqueQueryStrValue is an optional parameter that will make this query re-fire even when referendumOid has
// TODO: not changed.  Rip this out when a solution is found for #863
export const referendumVotesQuery = gql`
  query ReferendumVotesQuery ($referendumOid: String!, $uniqueQueryStrValue: Int) {
    getReferendumVotes (referendumOid: $referendumOid, uniqueQueryStrValue: $uniqueQueryStrValue)
    @rest (type: "ReferendumVotes", path: "/referendums/{args.referendumOid}/votes") {
      ...ReferendumVotes
    }
  }
  ${ReferendumVotesFragment}
`;
