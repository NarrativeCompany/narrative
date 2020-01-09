import gql from 'graphql-tag';
import { NicheFragment } from '../fragments/nicheFragment';

export const similarNichesByNicheIdQuery = gql`
  query SimilarNichesByNicheIdQuery ($input: SimilarNichesByNicheIdInput!) {
    getSimilarNichesByNicheId (input: $input) @rest (type: "Niche", path: "/niches/similar/{args.input.nicheId}") {
      ...Niche
    }
  }
  ${NicheFragment}
`;
