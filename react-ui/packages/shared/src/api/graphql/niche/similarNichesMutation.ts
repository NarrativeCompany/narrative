import gql from 'graphql-tag';
import { NicheFragment } from '../fragments/nicheFragment';

export const similarNichesMutation = gql`
  mutation SimilarNichesMutation ($input: SimilarNichesInput!) {
    findSimilarNiches (input: $input) @rest (type: "Niche", path: "/niches/similar", method: "POST") {
      ...Niche
    }
  }
  ${NicheFragment}
`;
