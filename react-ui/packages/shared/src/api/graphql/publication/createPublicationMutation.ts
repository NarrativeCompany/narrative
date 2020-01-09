import gql from 'graphql-tag';
import { PublicationFragment } from '../fragments/publicationFragment';

export const createPublicationMutation = gql`
  mutation CreatePublicationMutation ($input: CreatePublicationInput!) {
    createPublication (input: $input) @rest(
      type: "Publication", 
      path: "/publications", 
      method: "POST"
    ) {
      ...Publication
    }
  }
  ${PublicationFragment}
`;
