import gql from 'graphql-tag';
import { ElectionFragment } from './electionFragment';
import { NicheFragment } from './nicheFragment';

export const NicheModeratorElectionFragment = gql`
  fragment NicheModeratorElection on NicheModeratorElection {
    oid
    election @type(name: "Election") {
      ...Election
    }
    niche @type(name: "Niche") {
      ...Niche
    }
  }
  ${ElectionFragment}
  ${NicheFragment}
`;
