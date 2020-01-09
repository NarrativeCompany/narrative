import gql from 'graphql-tag';
import { NicheModeratorElectionDetailFragment } from '../fragments/NicheModeratorElectionDetailFragment';

export const nicheModeratorElectionDetailsQuery = gql`
  query NicheModeratorElectionDetailsQuery ($electionOid: String!) {
    getNicheModeratorElectionDetail (electionOid: $electionOid)
    @rest(type: "NicheModeratorElectionDetail", path: "/niches/moderator-elections/{args.electionOid}") {
      ...NicheModeratorElectionDetail
    }
  }
  ${NicheModeratorElectionDetailFragment}
`;
