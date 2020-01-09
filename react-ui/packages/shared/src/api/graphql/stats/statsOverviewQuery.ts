import gql from 'graphql-tag';
import { StatsOverviewFragment } from '../fragments/statsOverviewFragment';

export const statsOverviewQuery = gql`
  query StatsOverviewQuery {
    getStatsOverview @rest(type: "StatsOverview", path: "/stats/overview") {
      ...StatsOverview
    }
  }
  ${StatsOverviewFragment}
`;
