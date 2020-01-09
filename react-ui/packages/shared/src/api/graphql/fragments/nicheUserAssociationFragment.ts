import gql from 'graphql-tag';
import { NicheFragment } from './nicheFragment';

export const NicheUserAssociationFragment = gql`
  fragment NicheUserAssociation on NicheUserAssociation {
    niche @type(name: "Niche") {
      ...Niche
    }
    type
    associationSlot
    associationDatetime
  }
  ${NicheFragment}
`;
