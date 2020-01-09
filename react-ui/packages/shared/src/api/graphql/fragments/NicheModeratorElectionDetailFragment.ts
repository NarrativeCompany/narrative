import gql from 'graphql-tag';
import { ElectionDetailFragment } from './electionDetailFragment';
import { NicheFragment } from './nicheFragment';

export const NicheModeratorElectionDetailFragment = gql`
  fragment NicheModeratorElectionDetail on NicheModeratorElectionDetail {
    oid
    election @type(name: "ElectionDetail") {
      ...ElectionDetail
    }
    niche @type(name: "Niche") {
      ...Niche
    }
  }
  ${ElectionDetailFragment}
  ${NicheFragment}
`;
