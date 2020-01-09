import gql from 'graphql-tag';
import { NicheStatsFragment } from '../fragments/nicheStatsFragment';

export const nicheStatsQuery = gql`
  query NicheStatsQuery {
    getNicheStats @rest(type: "NicheStats", path: "/stats/niches") {
      ...NicheStats
    }
  }
  ${NicheStatsFragment}
`;
