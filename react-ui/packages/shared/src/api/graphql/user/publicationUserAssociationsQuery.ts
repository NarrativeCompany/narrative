import gql from 'graphql-tag';
import { PublicationUserAssociationFragment } from '../fragments/publicationUserAssociationFragment';

export const publicationUserAssociationsQuery = gql`
  query PublicationUserAssociationsQuery ($userOid: String!) {
    getPublicationUserAssociations (userOid: $userOid)
    @rest (type: "PublicationUserAssociation", path: "/users/{args.userOid}/publication-associations") {
      ...PublicationUserAssociation
    }
  }
  ${PublicationUserAssociationFragment}
`;
