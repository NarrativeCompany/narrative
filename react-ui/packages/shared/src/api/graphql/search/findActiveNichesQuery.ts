import gql from 'graphql-tag';
import { NicheFragment } from '../fragments/nicheFragment';

export const findActiveNichesQuery = gql`
  query FindActiveNichesQuery ($input: FindActiveNichesInput!) {
    findActiveNiches (input: $input) @rest(type: "Niche", path: "/search/active-niches?{args.input}") {
      ...Niche
    }
  }
  ${NicheFragment}
`;
