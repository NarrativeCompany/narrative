import gql from 'graphql-tag';
import { ReferendumFragment } from '../fragments/referendumFragment';

export const createNicheMutation = gql`
  mutation CreateNicheMutation ($input: CreateNicheInput!) {
    createNiche (input: $input) @rest(type: "Referendum", path: "/niches" method: "POST") {
      ...Referendum
    }
  }
  ${ReferendumFragment}
`;
