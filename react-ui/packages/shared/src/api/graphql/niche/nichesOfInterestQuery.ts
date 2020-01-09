import gql from 'graphql-tag';
import { NicheFragment } from '../fragments/nicheFragment';

export const nichesOfInterestQuery = gql`
  query NichesOfInterestQuery {
    getNichesOfInterest @rest(type: "Niche", path: "/niches/interest") {
      ...Niche
    }
  }
  ${NicheFragment}
`;
