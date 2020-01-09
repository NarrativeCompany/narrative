import gql from 'graphql-tag';
import { PublicationPowerUsersFragment } from '../fragments/publicationPowerUsersFragment';

export const deletePublicationPowerUserMutation = gql`
  mutation DeletePublicationPowerUserMutation ($input: DeletePublicationPowerUserInput!) {
    deletePublicationPowerUser (input: $input) @rest(
      type: "PublicationPowerUsers", 
      path: "/publications/{args.input.publicationOid}/power-users/{args.input.userOid}/{args.input.role}", 
      method: "DELETE"
    ) {
      ...PublicationPowerUsers
    }
  }
  ${PublicationPowerUsersFragment}
`;
