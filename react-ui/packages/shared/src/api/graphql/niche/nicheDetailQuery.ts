import gql from 'graphql-tag';
import { NicheDetailFragment } from '../fragments/nicheDetailFragment';

export const nicheDetailQuery = gql`
  query NicheDetailQuery ($nicheId: String!) {
    getNicheDetail (nicheId: $nicheId) 
    @rest(
      type: "NicheDetail", 
      path: "/niches/{args.nicheId}"
    ) {
      ...NicheDetail
    }
  }
  ${NicheDetailFragment}
`;
