import gql from 'graphql-tag';
import { ElectionDetailFragment } from '../fragments/electionDetailFragment';

export const nominateCurrentUserMutation = gql`
  mutation NominateCurrentUserMutation ($input: NominateCurrentUserInput, $electionOid: String!) {
    nominateCurrentUser (input: $input, electionOid: $electionOid)
    @rest(type: "ElectionDetail", path: "/elections/{args.electionOid}/nominees/current-user", method: "PUT") {
      ...ElectionDetail
    }
  }
  ${ElectionDetailFragment}
`;
