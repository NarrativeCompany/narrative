import gql from 'graphql-tag';
import { PublicationPowerUsersFragment } from '../fragments/publicationPowerUsersFragment';

export const invitePublicationPowerUserMutation = gql`
  mutation InvitePublicationPowerUserMutation (
    $input: InvitePublicationPowerUserInput!, 
    $publicationOid: String!, 
    $userOid: String!
  ) {
    invitePublicationPowerUser (input: $input, publicationOid: $publicationOid, userOid: $userOid) @rest(
      type: "PublicationPowerUsers", 
      path: "/publications/{args.publicationOid}/power-users/{args.userOid}/invites", 
      method: "POST"
    ) {
      ...PublicationPowerUsers
    }
  }
  ${PublicationPowerUsersFragment}
`;
