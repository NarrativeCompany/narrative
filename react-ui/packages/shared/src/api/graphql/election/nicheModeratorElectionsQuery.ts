import gql from 'graphql-tag';
import { NicheModeratorElectionFragment } from '../fragments/nicheModeratorElectionFragment';
import { PageInfoFragment } from '../fragments/pageInfoFragment';

export const nicheModeratorElectionsQuery = gql`
  query NicheModeratorElectionsQuery ($size: Int, $page: Int) {
    getNicheModeratorElections (size: $size, page: $page)
    @rest(type: "NicheModeratorElectionPayload", path: "/niches/moderator-elections?{args}") {
      items @type(name: "NicheModeratorElection") {
        ...NicheModeratorElection
      }
      info @type(name: "PageInfo") {
        ...PageInfo
      }
    }
  }
  ${NicheModeratorElectionFragment}
  ${PageInfoFragment}
`;
