import gql from 'graphql-tag';
import { ElectionNomineeFragment } from '../fragments/electionNomineeFragment';

export const electionNomineesQuery = gql`
  query ElectionNomineesQuery ($input: ElectionNomineesQueryInput, $electionOid: String!) {
    getElectionNominees (input: $input, electionOid: $electionOid)
    @rest (type: "ElectionNominees", path: "/elections/{args.electionOid}/nominees?{args.input}") {
      items @type(name: "ElectionNominee") {
        ...ElectionNominee
      }
      hasMoreItems
      lastItemConfirmationDatetime
    }
  }
  ${ElectionNomineeFragment}
`;
