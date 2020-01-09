import gql from 'graphql-tag';
import { ElectionDetailFragment } from '../fragments/electionDetailFragment';

export const removeCurrentUserNominationMutation = gql`
  mutation RemoveCurrentUserNominationMutation ($electionOid: String!) {
    removeCurrentUserNomination (electionOid: $electionOid)
    @rest(type: "ElectionDetail", path: "/elections/{args.electionOid}/nominees/current-user", method: "DELETE") {
      ...ElectionDetail
    }
  }
  ${ElectionDetailFragment}
`;
