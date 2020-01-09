import gql from 'graphql-tag';
import { NicheFragment } from '../fragments/nicheFragment';

export const trendingNichesQuery = gql`
  query TrendingNichesQuery {
    getTrendingNiches @rest(type: "Niche", path: "/niches/trending") {
      ...Niche
    }
  }
  ${NicheFragment}
`;
