import gql from 'graphql-tag';
import { NicheFragment } from '../fragments/nicheFragment';

export const nichesMostPostedToByCurrentUserQuery = gql`
  query NichesMostPostedToByCurrentUserQuery {
    getNichesMostPostedToByCurrentUser @rest(type: "Niche", path: "/users/current/niches-most-posted-to") {
      ...Niche
    }
  }
  ${NicheFragment}
`;
