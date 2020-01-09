import gql from 'graphql-tag';
import { NicheUserAssociationFragment } from '../fragments/nicheUserAssociationFragment';

export const nicheUserAssociationsQuery = gql`
  query NicheUserAssociationsQuery ($userOid: String!) {
    getNicheUserAssociations (userOid: $userOid)
    @rest (type: "NicheUserAssociation", path: "/users/{args.userOid}/niche-associations") {
      ...NicheUserAssociation
    }
  }
  ${NicheUserAssociationFragment}
`;
