import gql from 'graphql-tag';
import { PublicationFragment } from './publicationFragment';

export const PublicationUserAssociationFragment = gql`
  fragment PublicationUserAssociation on PublicationUserAssociation {
    oid
    publication @type(name: "Publication") {
      ...Publication
    }
    roles
    owner
  }
  ${PublicationFragment}
`;
